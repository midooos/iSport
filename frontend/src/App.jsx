import { BrowserRouter, Link, Route, Routes } from 'react-router-dom'
import Home from './pages/Home.jsx'

function App() {
  return (
    <BrowserRouter>
      <nav>
        <Link to="/">Accueil</Link>
      </nav>
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}

export default App
