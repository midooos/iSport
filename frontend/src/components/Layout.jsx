import { Outlet } from 'react-router-dom'
import Navbar from './Navbar.jsx'

function Layout() {
  return (
    <>
      <Navbar notifications={2} />
      <main>
        <Outlet />
      </main>
    </>
  )
}

export default Layout
