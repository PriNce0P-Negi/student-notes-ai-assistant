import { useState, useEffect, useCallback } from 'react'
import Sidebar from './components/Sidebar'
import ChatArea from './components/ChatArea'
import UploadModal from './components/UploadModal'
import AuthScreen from './components/AuthScreen'
import { createSession, getSessionDocuments } from './api/api'

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem('notesai_token') || null)
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('notesai_user')
    return savedUser ? JSON.parse(savedUser) : null
  })

  const [sessionId, setSessionId] = useState(() =>
    localStorage.getItem('notesai_session') || null
  )
  const [documents, setDocuments] = useState([])

  const [selectedDocIds, setSelectedDocIds] = useState([])

  const [showUpload, setShowUpload] = useState(false)

  useEffect(() => {
    if (sessionId) {
      getSessionDocuments(sessionId)
        .then(setDocuments)
        .catch(() => setDocuments([]))
    }
  }, [sessionId])

  const handleNewSession = useCallback(async () => {
    const newId = await createSession()
    localStorage.setItem('notesai_session', newId)
    setSessionId(newId)
    setDocuments([])
    setSelectedDocIds([])
  }, [])

  const handleUploadSuccess = useCallback(async (uploadedDoc) => {
    if (uploadedDoc.sessionId && !sessionId) {
      localStorage.setItem('notesai_session', uploadedDoc.sessionId)
      setSessionId(uploadedDoc.sessionId)
    }
    const docs = await getSessionDocuments(
      uploadedDoc.sessionId || sessionId
    )
    setDocuments(docs)
    setShowUpload(false)
  }, [sessionId])

  const handleLoginSuccess = (authData) => {
    setToken(authData.token)
    setUser({ name: authData.name, email: authData.email })
    localStorage.setItem('notesai_token', authData.token)
    localStorage.setItem('notesai_user', JSON.stringify({ name: authData.name, email: authData.email }))
  }

  const handleLogout = () => {
    setToken(null)
    setUser(null)
    setSessionId(null)
    setDocuments([])
    setSelectedDocIds([])
    localStorage.removeItem('notesai_token')
    localStorage.removeItem('notesai_user')
    localStorage.removeItem('notesai_session')
  }

  if (!token) {
    return <AuthScreen onLoginSuccess={handleLoginSuccess} />
  }

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 overflow-hidden">
      <div className="fixed inset-0 pointer-events-none">
        <div className="absolute top-0 left-1/3 w-96 h-96 bg-violet-600/8 rounded-full blur-3xl" />
        <div className="absolute bottom-1/4 right-1/4 w-64 h-64 bg-indigo-600/6 rounded-full blur-3xl" />
      </div>

      <Sidebar
        sessionId={sessionId}
        documents={documents}
        selectedDocIds={selectedDocIds}
        setSelectedDocIds={setSelectedDocIds}
        onNewSession={handleNewSession}
        onUploadClick={() => setShowUpload(true)}
        user={user}
        onLogout={handleLogout}
      />

      <ChatArea
        sessionId={sessionId}
        selectedDocIds={selectedDocIds}
        documents={documents}
        onNewSession={handleNewSession}
        onUploadClick={() => setShowUpload(true)}
      />

      {showUpload && (
        <UploadModal
          sessionId={sessionId}
          onSuccess={handleUploadSuccess}
          onClose={() => setShowUpload(false)}
        />
      )}
    </div>
  )
}
