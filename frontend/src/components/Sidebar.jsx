import { useState } from 'react'
import {
  Sparkles, Plus, FileText, Upload,
  CheckSquare, Square, BookOpen, Trash2,
  ChevronDown, ChevronRight
} from 'lucide-react'
import { clearHistory } from '../api/api'

function truncate(str, n) {
  return str && str.length > n ? str.slice(0, n) + '…' : str
}

function formatBytes(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1048576).toFixed(1) + ' MB'
}

function StatusBadge({ status }) {
  const styles = {
    READY:       'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    PROCESSING:  'bg-amber-500/20 text-amber-400 border-amber-500/30',
    FAILED_NO_TEXT: 'bg-red-500/20 text-red-400 border-red-500/30',
  }
  const labels = {
    READY: 'Ready',
    PROCESSING: 'Processing…',
    FAILED_NO_TEXT: 'No Text',
  }
  const cls = styles[status] || 'bg-slate-500/20 text-slate-400 border-slate-500/30'
  return (
    <span className={`badge border text-[10px] ${cls}`}>
      {labels[status] || status}
    </span>
  )
}

function DocRow({ doc, selected, onToggle }) {
  const checked = selected.includes(doc.id)
  return (
    <button
      onClick={() => onToggle(doc.id)}
      className={`w-full flex items-start gap-2.5 px-3 py-2.5 rounded-xl text-left
                  transition-all duration-150 group
                  ${checked
                    ? 'bg-violet-600/15 border border-violet-600/30'
                    : 'hover:bg-white/[0.05] border border-transparent'}`}
    >
      <div className="mt-0.5 shrink-0 text-violet-400">
        {checked
          ? <CheckSquare size={15} />
          : <Square size={15} className="text-slate-600 group-hover:text-slate-400" />}
      </div>
      <div className="min-w-0 flex-1">
        <p className={`text-xs font-medium leading-snug truncate
                       ${checked ? 'text-violet-300' : 'text-slate-300'}`}>
          {doc.originalFileName}
        </p>
        <div className="flex items-center gap-2 mt-1">
          <span className="text-[10px] text-slate-600">
            {formatBytes(doc.fileSize)}
          </span>
          <StatusBadge status={doc.uploadStatus} />
        </div>
      </div>
    </button>
  )
}

export default function Sidebar({
  sessionId,
  documents,
  selectedDocIds,
  setSelectedDocIds,
  onNewSession,
  onUploadClick,
  user,
  onLogout,
}) {
  const [docsExpanded, setDocsExpanded] = useState(true)
  const [clearing, setClearing] = useState(false)

  const toggleDoc = id => {
    setSelectedDocIds(prev =>
      prev.includes(id) ? prev.filter(d => d !== id) : [...prev, id]
    )
  }

  const toggleAll = () => {
    if (selectedDocIds.length === documents.length) {
      setSelectedDocIds([])
    } else {
      setSelectedDocIds(documents.map(d => d.id))
    }
  }

  const handleClearHistory = async () => {
    if (!sessionId || clearing) return
    if (!window.confirm('Clear all chat history for this session?')) return
    setClearing(true)
    try {
      await clearHistory(sessionId)
      window.location.reload()
    } finally {
      setClearing(false)
    }
  }

  const readyDocs = documents.filter(d => d.uploadStatus === 'READY')

  return (
    <aside className="flex flex-col w-72 shrink-0 border-r border-white/[0.06]
                       bg-slate-950 relative z-10">

      <div className="px-5 pt-6 pb-4 border-b border-white/[0.06]">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-violet-600 to-indigo-600
                          flex items-center justify-center shadow-lg shadow-violet-900/40">
            <Sparkles size={18} className="text-white" />
          </div>
          <div>
            <h1 className="text-sm font-bold text-white leading-none">Notes AI</h1>
            <p className="text-[11px] text-slate-500 mt-0.5">Study Assistant</p>
          </div>
        </div>
      </div>

      <div className="px-4 pt-4 pb-2">
        <p className="text-[10px] font-semibold text-slate-600 uppercase tracking-widest mb-2 px-1">
          Session
        </p>

        {sessionId ? (
          <div className="glass rounded-xl px-3 py-2.5 mb-2">
            <p className="text-[10px] text-slate-500 mb-0.5">Active Session</p>
            <p className="text-xs font-mono text-violet-400 truncate">
              {sessionId.slice(0, 20)}…
            </p>
          </div>
        ) : (
          <div className="glass rounded-xl px-3 py-2.5 mb-2 text-center">
            <p className="text-xs text-slate-500">No active session</p>
          </div>
        )}

        <button onClick={onNewSession} className="btn-ghost w-full justify-center text-xs mb-1">
          <Plus size={14} />
          New Session
        </button>
      </div>

      <div className="px-4 pt-2 flex-1 overflow-y-auto">
        <div className="flex items-center justify-between px-1 mb-2">
          <button
            onClick={() => setDocsExpanded(v => !v)}
            className="flex items-center gap-1.5 text-[10px] font-semibold
                       text-slate-600 uppercase tracking-widest hover:text-slate-400
                       transition-colors"
          >
            {docsExpanded ? <ChevronDown size={11} /> : <ChevronRight size={11} />}
            Documents ({readyDocs.length})
          </button>

          {documents.length > 0 && (
            <button
              onClick={toggleAll}
              className="text-[10px] text-violet-500 hover:text-violet-400 transition-colors"
            >
              {selectedDocIds.length === documents.length ? 'Deselect all' : 'Select all'}
            </button>
          )}
        </div>

        {docsExpanded && (
          <div className="space-y-1 mb-3">
            {documents.length === 0 ? (
              <div className="text-center py-8">
                <BookOpen size={28} className="text-slate-700 mx-auto mb-2" />
                <p className="text-xs text-slate-600">No documents yet</p>
                <p className="text-[11px] text-slate-700 mt-0.5">Upload a PDF to begin</p>
              </div>
            ) : (
              documents.map(doc => (
                <DocRow
                  key={doc.id}
                  doc={doc}
                  selected={selectedDocIds}
                  onToggle={toggleDoc}
                />
              ))
            )}
          </div>
        )}
      </div>

      <div className="px-4 pb-5 pt-2 border-t border-white/[0.06] space-y-1.5">
        {selectedDocIds.length > 0 && (
          <div className="glass rounded-xl px-3 py-2 mb-2">
            <p className="text-[11px] text-violet-400">
              <span className="font-semibold">{selectedDocIds.length}</span> doc
              {selectedDocIds.length > 1 ? 's' : ''} selected for chat
            </p>
            <p className="text-[10px] text-slate-600 mt-0.5">
              AI will only search these files
            </p>
          </div>
        )}

        <button
          onClick={onUploadClick}
          disabled={!sessionId}
          className="btn-primary w-full justify-center"
        >
          <Upload size={15} />
          Upload PDF
        </button>

        {sessionId && (
          <button
            onClick={handleClearHistory}
            disabled={clearing}
            className="btn-danger w-full justify-center"
          >
            <Trash2 size={14} />
            {clearing ? 'Clearing…' : 'Clear History'}
          </button>
        )}

        {user && (
          <div className="pt-2 mt-2 border-t border-white/[0.06] flex items-center justify-between">
            <div className="flex items-center gap-2 min-w-0">
              <div className="w-7 h-7 rounded-full bg-violet-600/20 text-violet-300 flex items-center justify-center text-[10px] font-bold shrink-0">
                {user.name ? user.name.charAt(0).toUpperCase() : 'U'}
              </div>
              <div className="min-w-0">
                <p className="text-[11px] font-medium text-slate-200 truncate">{user.name}</p>
                <p className="text-[9px] text-slate-500 truncate">{user.email}</p>
              </div>
            </div>
            <button
              onClick={onLogout}
              className="p-1.5 text-slate-500 hover:text-slate-300 hover:bg-white/[0.07] rounded-lg transition-colors shrink-0"
              title="Logout"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
            </button>
          </div>
        )}
      </div>
    </aside>
  )
}
