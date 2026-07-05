import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { Bot, User } from 'lucide-react'
import SourceCard from './SourceCard'

function UserMessage({ content }) {
  return (
    <div className="flex justify-end animate-slide-up">
      <div className="flex items-end gap-2.5 max-w-[75%]">
        <div className="bg-violet-600/90 text-white px-4 py-3 rounded-2xl rounded-br-sm
                        text-sm leading-relaxed shadow-lg shadow-violet-900/30">
          {content}
        </div>
        <div className="w-8 h-8 rounded-xl bg-slate-700 flex items-center justify-center shrink-0">
          <User size={15} className="text-slate-400" />
        </div>
      </div>
    </div>
  )
}

function AssistantMessage({ content, sources, isError }) {
  return (
    <div className="flex items-start gap-3 animate-slide-up">
      <div className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 mt-0.5
                       ${isError
                         ? 'bg-red-500/20 border border-red-500/30'
                         : 'bg-gradient-to-br from-violet-600 to-indigo-700 shadow-lg shadow-violet-900/40'}`}>
        <Bot size={15} className={isError ? 'text-red-400' : 'text-white'} />
      </div>

      <div className="flex-1 min-w-0 max-w-[85%]">
        <div className={`glass rounded-2xl rounded-tl-sm px-5 py-4
                         ${isError ? 'border-red-500/20' : ''}`}>
          <div className="prose-ai">
            <ReactMarkdown
              remarkPlugins={[remarkGfm]}
              components={{
                code({ node, inline, className, children, ...props }) {
                  if (inline) {
                    return (
                      <code className="bg-slate-700/60 text-violet-300 text-xs px-1.5 py-0.5 rounded font-mono" {...props}>
                        {children}
                      </code>
                    )
                  }
                  return (
                    <pre className="bg-slate-800/80 border border-slate-700/50 rounded-xl p-4 overflow-x-auto my-3">
                      <code className="text-slate-200 text-xs font-mono" {...props}>
                        {children}
                      </code>
                    </pre>
                  )
                },
                table({ children }) {
                  return (
                    <div className="overflow-x-auto my-3">
                      <table className="w-full border-collapse text-xs">{children}</table>
                    </div>
                  )
                },
                th({ children }) {
                  return (
                    <th className="text-left text-slate-400 font-semibold
                                   border-b border-slate-700/50 pb-2 pr-4 whitespace-nowrap">
                      {children}
                    </th>
                  )
                },
                td({ children }) {
                  return (
                    <td className="text-slate-300 border-b border-slate-800/50 py-1.5 pr-4">
                      {children}
                    </td>
                  )
                },
              }}
            >
              {content}
            </ReactMarkdown>
          </div>
        </div>

        {sources && sources.length > 0 && (
          <div className="mt-2.5 flex flex-wrap gap-2">
            <span className="text-[10px] text-slate-600 self-center">Sources:</span>
            {sources.map((src, i) => (
              <SourceCard key={i} source={src} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default function Message({ message }) {
  if (message.role === 'user') {
    return <UserMessage content={message.content} />
  }
  return (
    <AssistantMessage
      content={message.content}
      sources={message.sources}
      isError={message.isError}
    />
  )
}
