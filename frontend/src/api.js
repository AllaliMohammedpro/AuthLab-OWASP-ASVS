let csrfToken = null
export async function refreshCsrf() {
  const response = await fetch('/api/auth/csrf', {credentials: 'same-origin', cache: 'no-store'})
  if (!response.ok) throw new Error('Impossible de contacter le serveur Spring Boot.')
  csrfToken = (await response.json()).token
}
export async function api(path, options = {}, retry = true) {
  const method = options.method || 'GET'
  if (method !== 'GET' && !csrfToken) await refreshCsrf()
  const response = await fetch(`/api/auth${path}`, {
    ...options, credentials: 'same-origin', cache:'no-store',
    headers: {'Content-Type':'application/json', ...(method !== 'GET' ? {'X-CSRF-TOKEN':csrfToken} : {}), ...options.headers},
    ...(options.body ? {body: JSON.stringify(options.body)} : {})
  })
  if (response.status === 403 && method !== 'GET' && retry) {
    // Un ancien jeton (ex.: session expirée) est remplacé par un nouveau jeton CSRF.
    await refreshCsrf(); return api(path, options, false)
  }
  let data
  try { data = await response.json() } catch { data = {} }
  if (!response.ok) throw new Error(data.message || `Erreur HTTP ${response.status}`)
  if (path === '/logout') csrfToken = null
  return data
}
