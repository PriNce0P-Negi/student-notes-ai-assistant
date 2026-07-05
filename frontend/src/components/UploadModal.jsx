import { useState, useRef, useCallback } from 'react'
import { X, Upload, FileText, CheckCircle, AlertCircle, Loader2 } from 'lucide-react'
import { uploadDocument, createSession } from '../api/api'

function formatBytes(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1048576).toFixed(2) + ' MB'
}

function FileRow({ file, progress, status, error }) {
  return (
    <div className="glass rounded-xl p-3">
      <div className="flex items-start gap-3">
        <FileText size={18} className="text-violet-400 shrink-0 mt-0.5" />
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-slate-200 truncate">{file.name}</p>
          <p className="text-[11px] text-slate-500 mt-0.5">{formatBytes(file.size)}</p>

          {status === 'uploading' && (
            <div className="mt-2">
              <div className="h-1 bg-slate-700 rounded-full overflow-hidden">
                <div
                  className="h-full bg-violet-500 rounded-full transition-all duration-300"
                  style={{ width: `${progress}%` }}
                />
              </div>
              <p className="text-[10px] text-slate-500 mt-1">{progress}% — Uploading…</p>
            </div>
          )}

          {status === 'processing' && (
            <div className="flex items-center gap-1.5 mt-2">
              <Loader2 size={11} className="text-amber-400 animate-spin-slow" />
              <span className="text-[11px] text-amber-400">Processing & embedding…</span>
            </div>
          )}

          {status === 'done' && (
            <div className="flex items-center gap-1.5 mt-2">
              <CheckCircle size={11} className="text-emerald-400" />
              <span className="text-[11px] text-emerald-400">Ready for chat!</span>
            </div>
          )}

          {status === 'error' && (
            <div className="flex items-center gap-1.5 mt-2">
              <AlertCircle size={11} className="text-red-400" />
              <span className="text-[11px] text-red-400">{error || 'Upload failed'}</span>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default function UploadModal({ sessionId, onSuccess, onClose }) {
  const [dragging, setDragging] = useState(false)
  const [files, setFiles] = useState([])
  const [uploading, setUploading] = useState(false)
  const inputRef = useRef(null)

  const addFiles = useCallback(newFiles => {
    const pdfs = Array.from(newFiles).filter(f => {
      if (f.type !== 'application/pdf') return false
      if (f.size > 50 * 1024 * 1024) return false
      return true
    })
    setFiles(prev => [
      ...prev,
      ...pdfs.map(f => ({ file: f, progress: 0, status: 'pending', error: null })),
    ])
  }, [])

  const onDragOver = e => { e.preventDefault(); setDragging(true) }
  const onDragLeave = e => { e.preventDefault(); setDragging(false) }
  const onDrop = e => {
    e.preventDefault()
    setDragging(false)
    addFiles(e.dataTransfer.files)
  }
  const onFileInput = e => addFiles(e.target.files)

  const removeFile = i =>
    setFiles(prev => prev.filter((_, idx) => idx !== i))

  const handleUpload = async () => {
    if (files.length === 0 || uploading) return
    setUploading(true)

    let targetSessionId = sessionId

    if (!targetSessionId) {
      targetSessionId = await createSession()
    }

    let lastResponse = null

    for (let i = 0; i < files.length; i++) {
      const entry = files[i]
      if (entry.status === 'done') continue

      setFiles(prev =>
        prev.map((f, idx) =>
          idx === i ? { ...f, status: 'uploading', progress: 0 } : f
        )
      )

      try {
        const data = await uploadDocument(
          entry.file,
          targetSessionId,
          progress =>
            setFiles(prev =>
              prev.map((f, idx) => (idx === i ? { ...f, progress } : f))
            )
        )

        setFiles(prev =>
          prev.map((f, idx) =>
            idx === i ? { ...f, status: 'done', progress: 100 } : f
          )
        )
        lastResponse = data
      } catch (err) {
        const msg = err.response?.data?.message || err.message || 'Upload failed'
        setFiles(prev =>
          prev.map((f, idx) =>
            idx === i ? { ...f, status: 'error', error: msg } : f
          )
        )
      }
    }

    setUploading(false)

    if (lastResponse) {
      setTimeout(() => onSuccess(lastResponse), 800)
    }
  }

  const pendingCount = files.filter(f => f.status !== 'done').length
  const allDone = files.length > 0 && files.every(f => f.status === 'done')

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4
                 bg-black/60 backdrop-blur-sm animate-fade-in"
      onClick={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="w-full max-w-lg bg-slate-900 border border-white/[0.1] rounded-2xl
                      shadow-2xl shadow-black/50 animate-slide-up overflow-hidden">

        <div className="flex items-center justify-between px-6 py-4 border-b border-white/[0.07]">
          <div>
            <h3 className="text-base font-semibold text-white">Upload PDFs</h3>
            <p className="text-[11px] text-slate-500 mt-0.5">
              Max 50 MB per file · PDF format only
            </p>
          </div>
          <button onClick={onClose} className="btn-ghost p-1.5 rounded-lg">
            <X size={18} />
          </button>
        </div>

        <div className="p-6">
          <div
            onDragOver={onDragOver}
            onDragLeave={onDragLeave}
            onDrop={onDrop}
            onClick={() => inputRef.current?.click()}
            className={`border-2 border-dashed rounded-2xl px-6 py-10 text-center cursor-pointer
                        transition-all duration-200
                        ${dragging
                          ? 'border-violet-500 bg-violet-500/10'
                          : 'border-slate-700/50 hover:border-slate-600 hover:bg-white/[0.02]'}`}
          >
            <Upload size={28}
              className={`mx-auto mb-3 transition-colors
                          ${dragging ? 'text-violet-400' : 'text-slate-600'}`}
            />
            <p className="text-sm font-medium text-slate-300">
              {dragging ? 'Drop your PDF here' : 'Drop PDFs here or click to browse'}
            </p>
            <p className="text-[11px] text-slate-600 mt-1">
              Multiple files supported
            </p>
            <input
              ref={inputRef}
              type="file"
              accept="application/pdf"
              multiple
              className="hidden"
              onChange={onFileInput}
            />
          </div>

          {files.length > 0 && (
            <div className="mt-4 space-y-2 max-h-52 overflow-y-auto">
              {files.map((entry, i) => (
                <div key={i} className="relative group">
                  <FileRow
                    file={entry.file}
                    progress={entry.progress}
                    status={entry.status}
                    error={entry.error}
                  />
                  {entry.status === 'pending' && (
                    <button
                      onClick={() => removeFile(i)}
                      className="absolute top-2 right-2 text-slate-600 hover:text-slate-300
                                 opacity-0 group-hover:opacity-100 transition-opacity"
                    >
                      <X size={14} />
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="flex items-center justify-between px-6 py-4 border-t border-white/[0.07]">
          <button onClick={onClose} className="btn-ghost">
            {allDone ? 'Done' : 'Cancel'}
          </button>

          {!allDone && (
            <button
              onClick={handleUpload}
              disabled={pendingCount === 0 || uploading}
              className="btn-primary"
            >
              {uploading
                ? <><Loader2 size={15} className="animate-spin-slow" /> Uploading…</>
                : <><Upload size={15} /> Upload {pendingCount > 0 ? pendingCount : ''} File{pendingCount !== 1 ? 's' : ''}</>}
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
