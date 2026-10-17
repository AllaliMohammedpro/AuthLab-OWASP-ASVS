import React, { useState, useEffect } from 'react'
import { createRoot } from 'react-dom/client'
import { api, refreshCsrf } from './api'
import './style.css'
const controls = [
  ['6.1.1', 'Anti-automatisation', 'Limitation documentée des tentatives de connexion'],
  ['6.2.1', 'Longueur minimale', '8 caractères minimum, 15 recommandés'],
  ['6.2.2–3', 'Changement sécurisé', 'Ancien et nouveau mot de passe exigés'],
  ['6.2.4', 'Mots de passe courants', 'Liste locale des 3 000 mots de passe fréquents'],
  ['6.2.5–8', 'Saisie libre', 'Pas de composition imposée, copie autorisée'],
  ['6.3.1', 'Protection brute force', 'Limitation par adresse IP et compte'],
  ['6.3.2', 'Comptes par défaut', 'Aucun compte admin initial'],
  ['6.4.2', 'Questions secrètes', 'Aucune question secrète utilisée']
]
function Icon({name,size=20}) {
 const common={width:size,height:size,viewBox:'0 0 24 24',fill:'none',stroke:'currentColor',strokeWidth:1.8,strokeLinecap:'round',strokeLinejoin:'round'}
 const shapes={
  shield:<><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z"/><path d="m9 12 2 2 4-4"/></>,
  lock:<><rect x="5" y="10" width="14" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></>,
  eye:<><path d="M2 12s3.6-6 10-6 10 6 10 6-3.6 6-10 6-10-6-10-6Z"/><circle cx="12" cy="12" r="2.5"/></>,
  eyeOff:<><path d="m3 3 18 18M10 6.1A11 11 0 0 1 12 6c6.4 0 10 6 10 6a15 15 0 0 1-3.1 3.4M6.7 6.8C3.7 8.5 2 12 2 12s3.6 6 10 6c1.3 0 2.5-.25 3.5-.7"/></>,
  user:<><circle cx="12" cy="8" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2"/></>,
  logOut:<><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5M21 12H9"/></>,
  key:<><circle cx="8" cy="15" r="4"/><path d="m10.8 12.2 9-9 1 1-2 2 2 2-3 3-2-2-2 2"/></>,
  arrow:<><path d="M5 12h14m-6-6 6 6-6 6"/></>,
  check:<><path d="m5 12 4 4L19 6"/></>,
  info:<><circle cx="12" cy="12" r="10"/><path d="M12 11v5m0-8h.01"/></>,
  activity:<><path d="M2 12h4l3-7 5 14 3-7h5"/></>,
  chevron:<path d="m9 18 6-6-6-6"/>
 }
 return <svg aria-hidden="true" {...common}>{shapes[name]||shapes.shield}</svg>
}
function Logo({small=false}){return <div className="brand"><div className="brand-mark"><Icon name="shield" size={small?19:24}/></div><span>Auth<span className="brand-strong">Lab</span><span className="brand-dot">.</span></span></div>}
function PasswordInput({id,value,onChange,placeholder,autoComplete='new-password'}){
 const [visible,setVisible]=useState(false)
 return <div className="password-field"><input id={id} type={visible?'text':'password'} value={value} onChange={e=>onChange(e.target.value)} placeholder={placeholder} autoComplete={autoComplete} required/><button type="button" title={visible?'Masquer':'Afficher'} onClick={()=>setVisible(!visible)} className="eye-btn" aria-label={visible?'Masquer':'Afficher le mot de passe'}><Icon name={visible?'eyeOff':'eye'} size={18}/></button></div>
}
function Notice({message,error=false}){return message?<div role="status" className={`notice ${error?'notice-error':'notice-success'}`}><Icon name={error?'info':'check'} size={17}/><span>{message}</span></div>:null}
function AuthScreen({onLogin}){
 const [tab,setTab]=useState('login'),[busy,setBusy]=useState(false),[message,setMessage]=useState(''),[error,setError]=useState(false)
 const [email,setEmail]=useState(''),[name,setName]=useState(''),[password,setPassword]=useState('')
 function changeTab(next){setTab(next);setMessage('');setPassword('')}
 async function submit(e){e.preventDefault();setBusy(true);setMessage('')
  try{
   if(tab==='register'){
    const result=await api('/register',{method:'POST',body:{email,displayName:name,password}})
    changeTab('login');setMessage(result.message);setError(false)
   } else { const user=await api('/login',{method:'POST',body:{email,password}});onLogin(user) }
  }catch(e){setError(true);setMessage(e.message)}finally{setBusy(false)}
 }
 return <div className="auth-layout"><section className="auth-showcase"><div className="showcase-top"><Logo/><span className="subdued-label">OWASP ASVS 5.0.0 · L1</span></div><div className="hero"><span className="eyebrow"><span className="pulse-dot"/> LABORATOIRE DE SÉCURITÉ</span><h1>Apprendre à <em>protéger</em> les identités.</h1><p>Une application de démonstration pour comprendre, développer et tester les exigences d'authentification OWASP ASVS.</p><div className="hero-features"><div><span className="feature-icon"><Icon name="lock"/></span><div><strong>Sessions sécurisées</strong><small>Cookie HttpOnly et protection CSRF</small></div></div><div><span className="feature-icon"><Icon name="activity"/></span><div><strong>Défense anti-brute force</strong><small>Limitation des tentatives d'accès</small></div></div><div><span className="feature-icon"><Icon name="key"/></span><div><strong>Politique de mots de passe</strong><small>Contrôles ASVS 6.2</small></div></div></div></div><div className="showcase-footer">Projet pédagogique · Spring Boot + React <span>IRISI 3</span></div></section>
  <main className="auth-panel"><div className="mobile-logo"><Logo small/></div><div className="auth-card"><div className="panel-tag">ESPACE SÉCURISÉ <span>→</span></div><h2>{tab==='login'?'Bienvenue !':'Créer votre compte'}</h2><p className="auth-intro">{tab==='login'?'Connectez-vous pour accéder au tableau de bord de sécurité.':'Inscrivez-vous pour explorer les contrôles d’authentification.'}</p><div className="tab-switch"><button type="button" className={tab==='login'?'active':''} onClick={()=>changeTab('login')}>Connexion</button><button type="button" className={tab==='register'?'active':''} onClick={()=>changeTab('register')}>Inscription</button></div>
  <form onSubmit={submit} className="auth-form">{tab==='register'&&<div className="field"><label htmlFor="name">Nom affiché</label><input id="name" value={name} onChange={e=>setName(e.target.value)} maxLength={80} placeholder="Votre nom" autoComplete="name" required/></div>}<div className="field"><label htmlFor="email">Adresse e-mail</label><input id="email" type="email" value={email} onChange={e=>setEmail(e.target.value)} maxLength={254} placeholder="exemple@domaine.com" autoComplete="email" required/></div><div className="field"><label htmlFor="password">Mot de passe</label><PasswordInput id="password" value={password} onChange={setPassword} autoComplete={tab==='login'?'current-password':'new-password'} placeholder="Votre mot de passe"/></div>{tab==='register'&&<p className="password-hint"><Icon name="info" size={14}/> 8 caractères minimum, 15 recommandés. Aucun caractère spécial obligatoire.</p>}<Notice message={message} error={error}/><button className="primary-button" type="submit" disabled={busy}>{busy?'Veuillez patienter...':tab==='login'?'Se connecter':'Créer mon compte'}<Icon name="arrow" size={17}/></button></form><p className="auth-footnote"><Icon name="lock" size={14}/> Connexion sécurisée côté serveur · Pas de compte de démonstration prédéfini</p></div><div className="panel-bottom">TP 1 · Sécurité des applications et des services</div></main></div>
}
function Dashboard({user,onLogout}){
 const [view,setView]=useState('overview'),[oldPwd,setOldPwd]=useState(''),[newPwd,setNewPwd]=useState(''),[notice,setNotice]=useState(''),[failure,setFailure]=useState(false),[busy,setBusy]=useState(false)
 const since=new Intl.DateTimeFormat('fr-FR',{dateStyle:'long'}).format(new Date(user.createdAt))
 async function changePassword(e){e.preventDefault();setBusy(true);setNotice('');try{let result=await api('/change-password',{method:'POST',body:{currentPassword:oldPwd,newPassword:newPwd}});setOldPwd('');setNewPwd('');setFailure(false);setNotice(result.message)}catch(e){setFailure(true);setNotice(e.message)}finally{setBusy(false)}}
 async function logOut(){setBusy(true);try{await api('/logout',{method:'POST'});onLogout()}catch(e){setFailure(true);setNotice(e.message)}finally{setBusy(false)}}
 return <div className="dashboard"><aside className="sidebar"><Logo small/><p className="side-heading">NAVIGATION</p><button className={`side-link ${view==='overview'?'side-active':''}`} onClick={()=>{setView('overview');setNotice('')}}><Icon name="activity" size={18}/> Vue d'ensemble</button><button className={`side-link ${view==='password'?'side-active':''}`} onClick={()=>{setView('password');setNotice('')}}><Icon name="key" size={18}/> Mot de passe</button><button className={`side-link ${view==='controls'?'side-active':''}`} onClick={()=>{setView('controls');setNotice('')}}><Icon name="shield" size={18}/> Contrôles ASVS</button><div className="side-bottom"><div className="side-user"><span className="avatar">{user.displayName.charAt(0).toUpperCase()}</span><span><strong>{user.displayName}</strong><small>Compte utilisateur</small></span></div><button className="signout" onClick={logOut} disabled={busy}><Icon name="logOut" size={17}/> Déconnexion</button></div></aside>
 <main className="dash-main"><header className="dash-header"><span><span className="breadcrumbs">AuthLab</span> <Icon name="chevron" size={14}/> {view==='overview'?'Tableau de bord':view==='password'?'Mon mot de passe':'Contrôles ASVS'}</span><span className="status-pill"><span className="green-dot"/> Session active</span></header><section className="dash-content"><div className="page-kicker">TP 1 · AUTHENTIFICATION SÉCURISÉE</div>
 {view==='overview'&&<><h1>Bonjour, {user.displayName.split(' ')[0]} <span className="wave">✳</span></h1><p className="page-subtitle">Votre espace de démonstration est prêt. Découvrez les mécanismes de sécurité mis en place.</p><div className="metrics"><div className="metric"><div className="metric-icon mint"><Icon name="shield" size={23}/></div><span>ÉTAT DU COMPTE</span><strong>Protégé</strong><small>Authentification par session</small></div><div className="metric"><div className="metric-icon blue"><Icon name="lock" size={23}/></div><span>CONTRÔLE D'ACCÈS</span><strong>Actif</strong><small>API privée sécurisée</small></div><div className="metric"><div className="metric-icon violet"><Icon name="activity" size={23}/></div><span>RÉFÉRENTIEL</span><strong>ASVS L1</strong><small>Version 5.0.0</small></div></div><div className="content-two"><div className="white-card"><div className="card-title"><h3>Profil utilisateur</h3><Icon name="user" size={19}/></div><div className="detail-row"><span>Nom</span><strong>{user.displayName}</strong></div><div className="detail-row"><span>Email</span><strong className="break-text">{user.email}</strong></div><div className="detail-row"><span>Inscription</span><strong>{since}</strong></div><div className="detail-row last"><span>Rôle</span><span className="mini-tag">USER</span></div></div><div className="white-card"><div className="card-title"><h3>À explorer</h3><Icon name="shield" size={19}/></div><p className="card-desc">Tester la sécurité d'une application consiste à vérifier les exigences, pas seulement à vérifier que la connexion fonctionne.</p><button className="explore-link" onClick={()=>setView('controls')}>Voir les exigences L1 <Icon name="arrow" size={17}/></button><div className="separator"/><button className="explore-link" onClick={()=>setView('password')}>Changer mon mot de passe <Icon name="arrow" size={17}/></button></div></div><div className="tip-banner"><span><Icon name="info" size={20}/></span><p><strong>À noter :</strong> cette application est un TP local. Elle ne remplace pas un audit ASVS complet ni une infrastructure de production.</p></div></>}
 {view==='password'&&<><h1>Sécurité du mot de passe</h1><p className="page-subtitle">Vérifiez votre ancien mot de passe avant de définir un nouveau secret.</p><div className="white-card form-card"><div className="card-title"><h3>Modifier mon mot de passe</h3><span className="mini-tag">ASVS 6.2.2 / 6.2.3</span></div><form onSubmit={changePassword} className="auth-form"><div className="field"><label htmlFor="old">Mot de passe actuel</label><PasswordInput id="old" value={oldPwd} onChange={setOldPwd} autoComplete="current-password" placeholder="Mot de passe actuel"/></div><div className="field"><label htmlFor="new">Nouveau mot de passe</label><PasswordInput id="new" value={newPwd} onChange={setNewPwd} autoComplete="new-password" placeholder="Nouveau mot de passe"/></div><p className="password-hint"><Icon name="info" size={15}/> 8 caractères minimum. Les phrases longues sont conseillées ; la composition est libre.</p><Notice message={notice} error={failure}/><button className="primary-button" type="submit" disabled={busy}>{busy?'Enregistrement...':'Enregistrer le nouveau mot de passe'}<Icon name="arrow" size={17}/></button></form></div></>}
 {view==='controls'&&<><h1>Exigences OWASP ASVS</h1><p className="page-subtitle">Un aperçu des contrôles du chapitre V6 appliqués dans cette démonstration.</p><div className="white-card controls-card"><div className="controls-header"><span>EXIGENCES APPLIQUÉES</span><span className="mini-tag">ASVS 5.0.0 · L1</span></div>{controls.map(([ref,title,detail])=><div className="control-row" key={ref}><span className="control-ref">{ref}</span><div><strong>{title}</strong><small>{detail}</small></div><span className="check-circle"><Icon name="check" size={15}/></span></div>)}</div><div className="tip-banner"><Icon name="info" size={20}/><p>6.4.1 n'est pas applicable : aucun code d'activation ou mot de passe initial n'est généré. Les contrôles doivent être vérifiés par des tests et une revue du code.</p></div></>}
 </section></main></div>
}
function App(){const [user,setUser]=useState(null),[loading,setLoading]=useState(true),[startupError,setStartupError]=useState('')
 useEffect(()=>{let cancelled=false; (async()=>{try{await refreshCsrf();const account=await api('/me');if(!cancelled)setUser(account)}catch(e){if(!cancelled&&(e.message.includes('contacter') || e.message.includes('fetch') || e instanceof TypeError))setStartupError('Le backend Spring Boot est inaccessible. Démarrez-le sur le port 8080.') }finally{if(!cancelled)setLoading(false)}})();return()=>{cancelled=true}},[])
 if(loading)return <div className="loading-page"><Logo/><p>Chargement du laboratoire sécurisé...</p></div>
 return <>{startupError&&<div className="startup-error"><Icon name="info" size={16}/>{startupError}</div>}{user?<Dashboard user={user} onLogout={()=>setUser(null)}/>:<AuthScreen onLogin={setUser}/>}</>
}
createRoot(document.getElementById('root')).render(<App/>)
