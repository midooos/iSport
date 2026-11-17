import StatusBadge from './StatusBadge.jsx'

function EventTable({ events }) {
  if (events.length === 0) {
    return <p>Aucun événement à venir.</p>
  }
  return (
    <table className="event-table">
      <thead>
        <tr>
          <th>Événement</th>
          <th>Lieu</th>
          <th>Participants</th>
          <th>Date</th>
          <th>Statut</th>
        </tr>
      </thead>
      <tbody>
        {events.map((e) => (
          <tr key={e.id}>
            <td>{e.title}</td>
            <td>{e.location}</td>
            <td>{e.participantsCount}/{e.maxParticipants}</td>
            <td>{e.date} à {e.time}</td>
            <td><StatusBadge status={e.status} /></td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}

export default EventTable
