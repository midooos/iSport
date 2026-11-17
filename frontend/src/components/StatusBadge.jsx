function StatusBadge({ status }) {
  const full = status === 'COMPLET'
  return (
    <span className={full ? 'badge badge-full' : 'badge badge-open'}>
      {full ? 'Complet' : 'Ouvert'}
    </span>
  )
}

export default StatusBadge
