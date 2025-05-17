import React, { useState } from 'react';
import { FaUser, FaLock, FaEye, FaEyeSlash } from 'react-icons/fa';
import axios from 'axios';
import '../index.css';

interface LoginProps {
    onLogin: (playerData: { id: number; email: string; name: string | null }) => void;
}

const Login: React.FC<LoginProps> = ({ onLogin }) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [showPassword, setShowPassword] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        console.log('Нажата клавиша Enter, отправка формы');
        setError('');

        try {
            console.log('Отправка запроса на авторизацию:', { email, password });
            const response = await axios.post('http://localhost:8080/players/login', {
                email,
                password,
            });
            console.log('Ответ от сервера:', response.data);
            onLogin(response.data);
        } catch (err) {
            console.error('Ошибка авторизации:', err);
            setError('Неверный email или пароль');
        }
    };

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <div className="login-header">
                    <FaUser className="login-icon" />
                    <h2>Вход в Chess</h2>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="input-group">
                        <FaUser className="input-icon" />
                        <input
                            type="email"
                            id="email"
                            name="email"
                            autoComplete="email"
                            placeholder="Email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>
                    <div className="input-group password-group">
                        <FaLock className="input-icon" />
                        <input
                            type={showPassword ? 'text' : 'password'}
                            id="password"
                            name="password"
                            autoComplete="current-password"
                            placeholder="Пароль"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                        <button
                            type="button"
                            className="password-toggle"
                            onClick={togglePasswordVisibility}
                        >
                            {showPassword ? <FaEyeSlash /> : <FaEye />}
                        </button>
                    </div>
                    {error && <div className="error-message">{error}</div>}
                    <button type="submit" className="login-button">
                        Войти
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Login;