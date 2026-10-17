package com.irisi.authlab.security;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.time.*;
import java.util.concurrent.ConcurrentHashMap;
/** Limiteur mémoire adapté à une démonstration mono-instance; aucune suspension permanente de compte. */
@Component
public class LoginRateLimiter {
 private static final Duration WINDOW=Duration.ofMinutes(5);
 private static final int PER_IP_AND_EMAIL=5, PER_IP=25;
 private final ConcurrentHashMap<String,Counter> counters=new ConcurrentHashMap<>();
 private static final class Counter { Instant from=Instant.now(); int failures; }
 private synchronized int count(String key){
  var c=counters.get(key);
  if(c==null)return 0;
  if(Duration.between(c.from,Instant.now()).compareTo(WINDOW)>=0){counters.remove(key);return 0;}
  return c.failures;
 }
 private synchronized void add(String key){
  if(counters.size()>10000) counters.entrySet().removeIf(e->Duration.between(e.getValue().from,Instant.now()).compareTo(WINDOW)>=0);
  // limite de mémoire pour cet exemple, même si un acteur envoie des identifiants variés
  if(counters.size()>20000 && !counters.containsKey(key)) return;
  var c=counters.computeIfAbsent(key,k->new Counter());
  if(Duration.between(c.from,Instant.now()).compareTo(WINDOW)>=0){c.from=Instant.now();c.failures=0;}
  c.failures++;
 }
 private String ip(HttpServletRequest req){return req.getRemoteAddr();} // Pas de confiance dans X-Forwarded-For
 private String identity(HttpServletRequest req,String email){return "PAIR:"+ip(req)+"/"+email;}
 public synchronized boolean blocked(HttpServletRequest req,String email){return count("IP:"+ip(req))>=PER_IP || count(identity(req,email))>=PER_IP_AND_EMAIL;}
 public synchronized void failed(HttpServletRequest req,String email){add("IP:"+ip(req));add(identity(req,email));}
 public synchronized void succeeded(HttpServletRequest req,String email){counters.remove(identity(req,email));}
}
