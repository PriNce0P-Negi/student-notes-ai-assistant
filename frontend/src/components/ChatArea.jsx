import { useState, useEffect, useRef } from 'react'
import { Send, Upload, Sparkles, Plus, Bot, Loader2, FileText } from 'lucide-react'
import { sendChat, getChatHistory } from '../api/api'
import Message from './Message'

function WelcomeScreen({ onNewSession }) {
  return (
    <div className="flex flex-col items-center justify-center h-full gap-8 px-8 animate-fade-in">
      <div className="relative">
        <div className="absolute inset-0 bg-violet-600/30 blur-2xl rounded-full scale-150" />
        <div className="relative w-20 h-20 rounded-2xl bg-gradient-to-br from-violet-600 to-indigo-700
                        flex items-center justify-center shadow-2xl shadow-violet-900/50">
          <Sparkles size={36} className="text-white" />
        </div>
      </div>

      <div className="text-center max-w-sm">
        <h2 className="text-2xl font-bold text-white mb-3">
          Student Notes AI
        </h2>
        <p className="text-slate-400 text-sm leading-relaxed">
          Upload your lecture notes, textbooks, or study materials
          and chat with them using AI-powered semantic search.
        </p>
      </div>

      <div className="flex flex-col gap-3 w-full max-w-xs">
        <button onClick={onNewSession} className="btn-primary justify-center py-3 text-base">
          <Plus size={18} />
          Start New Session
        </button>
        <p className="text-center text-[11px] text-slate-600">
          Each session groups your PDFs and conversation history together
        </p>
      </div>

      <div className="grid grid-cols-3 gap-4 max-w-md w-full mt-4">
        {[
          { icon: '📄', title: 'Upload PDFs', desc: 'Multiple files per session' },
          { icon: '🔍', title: 'Semantic Search', desc: 'Finds relevant passages' },
          { icon: '💬', title: 'Chat History', desc: 'Persists across restarts' },
        ].map(f => (
          <div key={f.title} className="glass rounded-xl p-3 text-center">
            <span className="text-2xl">{f.icon}</span>
            <p className="text-xs font-semibold text-white mt-1.5">{f.title}</p>
            <p className="text-[10px] text-slate-500 mt-0.5">{f.desc}</p>
          </div>
        ))}
      </div>
    </div>
  )
}

function EmptyChat({ onUploadClick, hasDocuments }) {
  const prompts = [
    'Summarize the main topics covered',
    'What are the key definitions?',
    'Explain the most important concept',
    'Create a study outline for this material',
  ]
  return (
    <div className="flex flex-col items-center justify-center h-full gap-6 px-8 animate-fade-in">
      <div className="w-14 h-14 rounded-2xl bg-slate-800/80 border border-slate-700/50
                      flex items-center justify-center">
        <Bot size={26} className="text-slate-500" />
      </div>
      <div className="text-center">
        <p className="text-slate-300 font-medium">
          {hasDocuments ? 'Ask anything about your notes' : 'Upload a PDF to get started'}
        </p>
        <p className="text-slate-600 text-sm mt-1">
          {hasDocuments
            ? 'Try one of the prompts below'
            : 'Your AI assistant is ready when you are'}
        </p>
      </div>

      {!hasDocuments && (
        <button onClick={onUploadClick} className="btn-primary">
          <Upload size={15} />
          Upload PDF
        </button>
      )}

      {hasDocuments && (
        <div className="grid grid-cols-2 gap-2 w-full max-w-lg">
          {prompts.map(p => (
            <button
              key={p}
              className="glass rounded-xl px-4 py-3 text-left text-xs text-slate-400
                         hover:text-slate-200 hover:border-violet-600/30 transition-all duration-150"
            >
              {p}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

function TypingIndicator() {
  return (
    <div className="flex items-end gap-3 animate-slide-up">
      <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-violet-600 to-indigo-700
                      flex items-center justify-center shrink-0">
        <Bot size={15} className="text-white" />
      </div>
      <div className="glass rounded-2xl rounded-bl-sm px-4 py-3">
        <div className="flex gap-1.5 items-center">
          <span className="w-1.5 h-1.5 bg-violet-400 rounded-full animate-bounce"
                style={{ animationDelay: '0ms' }} />
          <span className="w-1.5 h-1.5 bg-violet-400 rounded-full animate-bounce"
                style={{ animationDelay: '150ms' }} />
          <span className="w-1.5 h-1.5 bg-violet-400 rounded-full animate-bounce"
                style={{ animationDelay: '300ms' }} />
        </div>
      </div>
    </div>
  )
}

export default function ChatArea({
  sessionId,
  selectedDocIds,
  documents,
  onNewSession,
  onUploadClick,
}) {
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [historyLoaded, setHistoryLoaded] = useState(false)
  const bottomRef = useRef(null)
  const textareaRef = useRef(null)

  useEffect(() => {
    if (!sessionId) {
      setMessages([])
      setHistoryLoaded(false)
      return
    }
    setHistoryLoaded(false)
    getChatHistory(sessionId)
      .then(history => {
        const msgs = history.flatMap(h => [
          { role: 'user', content: h.question },
          { role: 'assistant', content: h.answer, sources: [] },
        ])
        setMessages(msgs)
        setHistoryLoaded(true)
      })
      .catch(() => setHistoryLoaded(true))
  }, [sessionId])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, loading])

  const handleSend = async () => {
    const question = input.trim()
    if (!question || !sessionId || loading) return

    setInput('')
    setMessages(prev => [...prev, { role: 'user', content: question }])
    setLoading(true)

    try {
      const data = await sendChat(sessionId, question, selectedDocIds)
      setMessages(prev => [
        ...prev,
        { role: 'assistant', content: data.answer, sources: data.sources || [] },
      ])
    } catch (err) {
      setMessages(prev => [
        ...prev,
        {
          role: 'assistant',
          content: '⚠️ Something went wrong. Please check your connection and try again.',
          sources: [],
          isError: true,
        },
      ])
    } finally {
      setLoading(false)
    }
  }

  const handleKeyDown = e => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  const handleInput = e => {
    setInput(e.target.value)
    const el = textareaRef.current
    if (el) {
      el.style.height = 'auto'
      el.style.height = Math.min(el.scrollHeight, 180) + 'px'
    }
  }

  const hasDocuments = documents.length > 0
  const selectedCount = selectedDocIds.length

  if (!sessionId) {
    return (
      <main className="flex-1 relative overflow-hidden">
        <WelcomeScreen onNewSession={onNewSession} />
      </main>
    )
  }

  return (
    <main className="flex flex-col flex-1 overflow-hidden relative">

      <header className="flex items-center justify-between px-6 py-4
                         border-b border-white/[0.06] shrink-0">
        <div>
          <h2 className="text-sm font-semibold text-white">
            Chat with your Notes
          </h2>
          <p className="text-[11px] text-slate-600 mt-0.5 font-mono">
            Session: {sessionId.slice(0, 16)}…
          </p>
        </div>

        <div className="flex items-center gap-2">
          {selectedCount > 0 ? (
            <div className="flex items-center gap-2">
              <div className="flex items-center gap-1.5 glass rounded-xl px-3 py-1.5">
                <FileText size={12} className="text-violet-400" />
                <span className="text-xs text-violet-300 font-medium">
                  {selectedCount} doc{selectedCount > 1 ? 's' : ''} selected
                </span>
              </div>
            </div>
          ) : (
            <div className="glass rounded-xl px-3 py-1.5">
              <span className="text-xs text-slate-500">
                {hasDocuments ? 'All documents' : 'No documents'}
              </span>
            </div>
          )}
        </div>
      </header>

      <div className="flex-1 overflow-y-auto px-6 py-6 space-y-6">
        {messages.length === 0 && historyLoaded && (
          <EmptyChat onUploadClick={onUploadClick} hasDocuments={hasDocuments} />
        )}

        {messages.map((msg, i) => (
          <Message key={i} message={msg} />
        ))}

        {loading && <TypingIndicator />}
        <div ref={bottomRef} />
      </div>

      <div className="px-6 py-4 border-t border-white/[0.06] shrink-0">
        <div className="flex items-end gap-3 glass rounded-2xl px-4 pt-3 pb-3">
          <textarea
            ref={textareaRef}
            rows={1}
            value={input}
            onChange={handleInput}
            onKeyDown={handleKeyDown}
            placeholder={
              hasDocuments
                ? 'Ask anything about your uploaded notes…'
                : 'Upload a PDF first, then ask questions…'
            }
            disabled={!hasDocuments || loading}
            className="flex-1 bg-transparent text-sm text-slate-100 placeholder:text-slate-600
                       resize-none focus:outline-none leading-relaxed py-0.5
                       disabled:opacity-50 disabled:cursor-not-allowed"
            style={{ minHeight: '24px', maxHeight: '180px', overflowY: 'auto' }}
          />

          <button
            onClick={onUploadClick}
            className="btn-ghost p-2 shrink-0"
            title="Upload PDF"
          >
            <Upload size={17} />
          </button>

          <button
            onClick={handleSend}
            disabled={!input.trim() || !hasDocuments || loading}
            className="btn-primary px-3 py-2 shrink-0"
          >
            {loading
              ? <Loader2 size={17} className="animate-spin-slow" />
              : <Send size={17} />}
          </button>
        </div>

        <p className="text-center text-[10px] text-slate-700 mt-2">
          Enter to send · Shift+Enter for new line ·
          {selectedCount > 0
            ? ` Searching ${selectedCount} selected doc${selectedCount > 1 ? 's' : ''}`
            : ' Searching all documents'}
        </p>
      </div>
    </main>
  )
}
