import { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [authToken, setAuthToken] = useState(() => localStorage.getItem('authToken'));
  const [authMode, setAuthMode] = useState('login');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [authError, setAuthError] = useState('');
  const [text, setText] = useState('');
  const [messages, setMessages] = useState([]);
  const [status, setStatus] = useState('');
  const API_URL = '/api/messages';
  const AUTH_URL = '/api/auth';

  useEffect(() => {
    if (!authToken) return;
    fetch(API_URL, { headers: { 'Authorization': `Bearer ${authToken}` } })
      .then(res => res.json())
      .then(data => setMessages(data))
      .catch(err => console.error('Ошибка загрузки:', err));
  }, [authToken]);

  useEffect(() => {
    if (!authToken) return;
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${wsProtocol}//${window.location.host}/ws/messages`;
    const socket = new WebSocket(wsUrl);
    socket.onmessage = (event) => {
      try {
        const newMessage = JSON.parse(event.data);
        setMessages((prev) => (prev.some(m => m.id === newMessage.id) ? prev : [newMessage, ...prev]));
      } catch (err) { console.error('WebSocket parse error:', err); }
    };
    return () => socket.close();
  }, [authToken]);

  if (!authToken) {
    return (
      <div className="App">
        <h1>{authMode === 'login' ? 'Вход' : 'Регистрация'}</h1>
        <form onSubmit={async (e) => {
          e.preventDefault();
          setAuthError('');
          try {
            const response = await fetch(`${AUTH_URL}/${authMode}`, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ username, password })
            });
            const data = await response.json();
            if (response.ok) {
              if (authMode === 'register') {
                setAuthError('✅ ' + data.message);
                setAuthMode('login');
              } else {
                localStorage.setItem('authToken', data.token);
                setAuthToken(data.token);
              }
            } else if (response.status === 403 && data.code === 'PENDING_APPROVAL') {
              setAuthError('⏳ ' + data.error);
            } else {
              setAuthError(data.error || 'Ошибка');
            }
          } catch (err) { setAuthError('Ошибка соединения'); }
        }}>
          <input type="text" placeholder="Логин" value={username} onChange={(e) => setUsername(e.target.value)} required />
          <input type="password" placeholder="Пароль" value={password} onChange={(e) => setPassword(e.target.value)} required />
          <button type="submit">{authMode === 'login' ? 'Войти' : 'Зарегистрироваться'}</button>
        </form>
        <p className="status">{authError}</p>
        <button onClick={() => { setAuthMode(authMode === 'login' ? 'register' : 'login'); setAuthError(''); }}>
          {authMode === 'login' ? 'Нет аккаунта? Зарегистрироваться' : 'Уже есть аккаунт? Войти'}
        </button>
      </div>
    );
  }

  return (
    <div className="App">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>Сообщения</h1>
        <button onClick={() => { localStorage.removeItem('authToken'); setAuthToken(null); }}>Выйти</button>
      </div>
      <form onSubmit={async (e) => {
        e.preventDefault();
        if (!text.trim()) return;
        try {
          const response = await fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${authToken}` },
            body: JSON.stringify({ text })
          });
          if (response.ok) { setStatus('Сохранено!'); setText(''); }
          else { const errData = await response.json(); setStatus(`Ошибка: ${errData.error || 'Не удалось сохранить'}`); }
        } catch (err) { setStatus('Ошибка соединения'); }
      }}>
        <input type="text" value={text} onChange={(e) => setText(e.target.value)} placeholder="Введите текст" />
        <button type="submit">Отправить</button>
      </form>
      <p className="status">{status}</p>
      <h2>Сохранённые сообщения:</h2>
      <ul>
        {messages.map(msg => (<li key={msg.id}><strong>{msg.text}</strong> <small>({msg.createdAt})</small></li>))}
      </ul>
    </div>
  );
}
export default App;