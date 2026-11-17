import EventTable from '../components/EventTable.jsx'
import { mockEvents } from '../data/mockEvents.js'

function Dashboard() {
  const joined = mockEvents.filter((e) => e.joined)
  const upcoming = mockEvents.filter((e) => !e.joined)

  return (
    <div>
      <h1>Bienvenue !</h1>
      <h2>Mes événements ({joined.length})</h2>
      <EventTable events={joined} />
      <h2>Événements à venir ({upcoming.length})</h2>
      <EventTable events={upcoming} />
    </div>
  )
}

export default Dashboard
