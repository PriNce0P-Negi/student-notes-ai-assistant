import { FileText } from 'lucide-react'

export default function SourceCard({ source }) {
  const name = source.documentName || 'Unknown document'
  const displayName = name.length > 28 ? name.slice(0, 26) + '…' : name

  return (
    <div
      title={`${name} — Page ${source.pageNumber ?? '?'}, Chunk ${source.chunkIndex ?? '?'}`}
      className="inline-flex items-center gap-1.5
                 bg-slate-800/60 hover:bg-slate-800 border border-slate-700/40
                 hover:border-violet-600/40 text-slate-400 hover:text-slate-300
                 rounded-lg px-2.5 py-1.5 text-[11px] cursor-default
                 transition-all duration-150"
    >
      <FileText size={11} className="text-violet-500 shrink-0" />
      <span className="font-medium truncate max-w-[180px]">{displayName}</span>
      {source.pageNumber != null && (
        <span className="text-slate-600 shrink-0">p.{source.pageNumber}</span>
      )}
    </div>
  )
}
