import { NavLink } from 'react-router-dom'

function Navbar({ notifications = 0 }) {
  return (
    <nav>
      <span className="brand">iSport</span>
      <NavLink to="/dashboard">Accueil</NavLink>
      <NavLink to="/new">Nouveau</NavLink>
      <NavLink to="/search">Recherche</NavLink>
      <NavLink to="/account" className="account-link">
        Compte
        {notifications > 0 && <span className="notif">{notifications}</span>}
      </NavLink>
    </nav>
  )
}

export default Navbar
