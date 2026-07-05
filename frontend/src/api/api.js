import axios from 'axios'

const BASE = import.meta.env.VITE_API_BASE || '/api'

const http = axios.create({
  baseURL: BASE,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('notesai_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, (error) => Promise.reject(error))

export const register = (name, email, password) =>
  http.post('/auth/register', { name, email, password }).then(r => r.data)

export const login = (email, password) =>
  http.post('/auth/authenticate', { email, password }).then(r => r.data)

export const createSession = () =>
  http.post('/sessions').then(r => r.data)

export const getSessionDocuments = sessionId =>
  http.get(`/sessions/${sessionId}/documents`).then(r => r.data)

export const getChatHistory = sessionId =>
  http.get(`/sessions/${sessionId}/history`).then(r => r.data)

export const clearHistory = sessionId =>
  http.delete(`/sessions/${sessionId}/history`).then(r => r.data)

export const uploadDocument = (file, sessionId, onProgress) => {
  const formData = new FormData()
  formData.append('file', file)

  return http
    .post(`/documents/upload?sessionId=${sessionId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: e => {
        if (onProgress && e.total) {
          onProgress(Math.round((e.loaded * 100) / e.total))
        }
      },
    })
    .then(r => r.data)
}

export const searchDocuments = (query, sessionId) =>
  http
    .get('/documents/search', { params: { query, sessionId } })
    .then(r => r.data)

export const sendChat = (sessionId, question, documentIds = []) =>
  http
    .post('/chat', { sessionId, question, documentIds })
    .then(r => r.data)

export const checkHealth = () =>
  http.get('/health').then(r => r.data)
