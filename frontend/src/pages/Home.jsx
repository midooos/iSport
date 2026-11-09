import { useEffect, useState } from 'react'
import { getHealth } from '../services/api.js'

function Home() {
  const [status, setStatus] = useState('vérification en cours...')

  useEffect(() => {
    getHealth()
      .then((data) => setStatus(data.status))
      .catch(() => setStatus('le back ne répond pas'))
  }, [])

  return (
    <div>
      <h1>iSport</h1>
      <p>Statut du back : {status}</p>
    </div>
  )
}

export default Home
