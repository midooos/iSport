import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout.jsx'
import Dashboard from './pages/Dashboard.jsx'

function Placeholder({ title }) {
  return <h1>{title} (à venir)</h1>
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/new" element={<Placeholder title="Nouvel événement" />} />
          <Route path="/search" element={<Placeholder title="Recherche" />} />
          <Route path="/account" element={<Placeholder title="Compte" />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
