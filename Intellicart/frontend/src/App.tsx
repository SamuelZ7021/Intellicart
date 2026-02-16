import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import OrderListPage from './pages/OrderListPage';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/orders" element={<OrderListPage />} />
        <Route path="/" element={<Navigate to="/orders" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
