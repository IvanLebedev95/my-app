import { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [text, setText] = useState('');
  const [messages, setMessages] = useState([]);
  const [status, setStatus] = useState('');

  const API_URL = '/api/messages';

  // Загрузка всех сообщений при монтировании
  useEffect(() => {
    fetch(API_URL)
      .then(res => res.json())
      .then(data => setMessages(data))
      .catch(err => console.error('Ошибка загрузки:', err));
  }, []);

  // НОВОЕ: Подключение к WebSocket для получения сообщений в реальном времени
  useEffect(() => {
    // Определяем протокол в зависимости от текущего (http/https)
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${wsProtocol}//${window.location.host}/ws/messages`;

    const socket = new WebSocket(wsUrl);

    socket.onopen = () => {
      console.log('WebSocket подключён');
    };

    socket.onmessage = (event) => {
      try {
        const newMessage = JSON.parse(event.data);
        // Добавляем сообщение в начало списка, избегая дубликатов
        setMessages((prev) => {
          if (prev.some((m) => m.id === newMessage.id)) {
            return prev;
          }
          return [newMessage, ...prev];
        });
      } catch (err) {
        console.error('Ошибка парсинга WebSocket-сообщения:', err);
      }
    };

    socket.onerror = (err) => {
      console.error('WebSocket ошибка:', err);
    };

    socket.onclose = () => {
      console.log('WebSocket закрыт');
    };

    // Закрываем соединение при размонтировании компонента
    return () => {
      socket.close();
    };
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!text.trim()) return;

    try {
      const response = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text })
      });
      if (response.ok) {
        setStatus('Сохранено успешно!');
        setText('');
        // ИЗМЕНЕНО: убрали повторный fetch(API_URL)
        // WebSocket сам пришлёт новое сообщение всем подключённым клиентам
      } else {
        const errData = await response.json();
        setStatus(`Ошибка: ${errData.error || 'Не удалось сохранить'}`);
      }
    } catch (err) {
      setStatus('Ошибка соединения с сервером');
    }
  };

  return (
    <div className="App">
      <h1>Отправка текста в базу данных</h1>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Введите текст"
        />
        <button type="submit">Отправить</button>
      </form>
      <p className="status">{status}</p>

      <h2>Сохранённые сообщения:</h2>
      <ul>
        {messages.map(msg => (
          <li key={msg.id}>
            <strong>{msg.text}</strong> <small>({msg.createdAt})</small>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default App;