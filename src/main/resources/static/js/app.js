// ============================================
// БЛОК 3: JAVASCRIPT (логика и работа с API)
// ============================================

// Глобальные переменные состояния
let currentPage = 0;       // Текущая страница пагинации
let pageSize = 10;         // Количество записей на странице
let editingId = null;      // ID редактируемого дрона (null если добавляем нового)

// При загрузке страницы сразу загружаем список дронов
window.onload = () => loadDrones();

// --- ЗАГРУЗКА ДАННЫХ ---
// Формирует URL с фильтрами, делает GET-запрос к API, отрисовывает таблицу
async function loadDrones() {
    const type = document.getElementById('filter-type').value;
    const manufacturer = document.getElementById('filter-manufacturer').value;
    const minWeight = document.getElementById('filter-minWeight').value;
    const minFlightTime = document.getElementById('filter-minFlightTime').value;

    let url = `/api/drones/filter?page=${currentPage}&size=${pageSize}`;
    if (type) url += `&type=${type}`;
    if (manufacturer) url += `&manufacturer=${encodeURIComponent(manufacturer)}`;
    if (minWeight) url += `&minWeight=${minWeight}`;
    if (minFlightTime) url += `&minFlightTime=${minFlightTime}`;

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error('Ошибка загрузки');

        const data = await response.json();
        renderTable(data.content);       // Рисуем таблицу
        renderPagination(data);          // Рисуем пагинацию
        hideError();
    } catch (error) {
        showError('Ошибка загрузки данных: ' + error.message);
    }
}

// --- ОТРИСОВКА ТАБЛИЦЫ ---
// Принимает массив дронов и создаёт HTML-строки (основная + раскрывающаяся с деталями)
function renderTable(drones) {
    const tbody = document.getElementById('drones-table');

    if (drones.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="loading">Ничего не найдено</td></tr>';
        return;
    }

    tbody.innerHTML = drones.map(drone => `
        <tr class="main-row">
            <td>${drone.id}</td>
            <td>${drone.name}</td>
            <td>${drone.manufacturer || '-'}</td>
            <td>${drone.type}</td>
            <td>${drone.maxTakeoffWeight || '-'}</td>
            <td>${drone.maxFlightTime || '-'}</td>
            <td>${drone.releaseYear || '-'}</td>
            <td>
                <button class="btn-toggle" onclick="toggleDetails(this)" title="Подробнее">▼</button>
                <button class="btn btn-warning" style="padding:4px 8px;" onclick="editDrone(${drone.id})">✏️</button>
                <button class="btn btn-danger" style="padding:4px 8px;" onclick="deleteDrone(${drone.id})">🗑️</button>
            </td>
        </tr>
        <tr class="details-row" style="display:none;">
            <td colspan="8">
                <div class="details-content">
                    <p style="margin:0 0 12px 0; line-height:1.5;">
                        <strong> Описание:</strong><br>
                        ${drone.description || 'Нет описания'}
                    </p>
                    <div class="details-grid">
                        <div><strong>📏 Дальность:</strong> ${drone.maxRange ? drone.maxRange + ' км' : '-'}</div>
                        <div><strong>📦 Грузоподъемность:</strong> ${drone.payloadCapacity ? drone.payloadCapacity + ' кг' : '-'}</div>
                        <div><strong>📡 Сенсоры:</strong> ${drone.sensorType || '-'}</div>
                        <div><strong>🛡️ Класс защиты:</strong> ${drone.ipRating || '-'}</div>
                    </div>
                </div>
            </td>
        </tr>
    `).join('');
}

// --- РАСКРЫТИЕ/СКРЫТИЕ ДЕТАЛЕЙ ---
// Переключает видимость строки с подробной информацией о дроне
function toggleDetails(btn) {
    const mainRow = btn.closest('tr');
    const detailsRow = mainRow.nextElementSibling;

    if (detailsRow && detailsRow.classList.contains('details-row')) {
        const isHidden = detailsRow.style.display === 'none' || detailsRow.style.display === '';
        detailsRow.style.display = isHidden ? 'table-row' : 'none';

        // Меняем иконку и цвет кнопки
        btn.textContent = isHidden ? '▲' : '▼';
        btn.classList.toggle('active', isHidden);
    }
}

// --- ОТРИСОВКА ПАГИНАЦИИ ---
// Создаёт кнопки "Назад" и "Вперёд" с учётом текущей страницы
function renderPagination(data) {
    const pagination = document.getElementById('pagination');

    if (data.totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }

    pagination.innerHTML = `
        <button class="btn btn-secondary" onclick="changePage(${currentPage - 1})" ${currentPage === 0 ? 'disabled' : ''}>← Назад</button>
        <span>Страница ${currentPage + 1} из ${data.totalPages}</span>
        <button class="btn btn-secondary" onclick="changePage(${currentPage + 1})" ${currentPage >= data.totalPages - 1 ? 'disabled' : ''}>Вперед →</button>
    `;
}

// Переключение на другую страницу
function changePage(page) {
    currentPage = page;
    loadDrones();
}

// --- МОДАЛЬНОЕ ОКНО ---
// Открывает форму добавления или редактирования (если передан id)
function openModal(id = null) {
    editingId = id;
    document.getElementById('modal-title').textContent = id ? 'Редактировать дрона' : 'Добавить дрона';
    document.getElementById('drone-form').reset();
    document.getElementById('drone-id').value = '';

    // Если редактируем — загружаем текущие данные дрона с сервера
    if (id) {
        fetch(`/api/drones/${id}`)
            .then(res => res.json())
            .then(drone => {
                document.getElementById('drone-id').value = drone.id;
                document.getElementById('drone-name').value = drone.name;
                document.getElementById('drone-manufacturer').value = drone.manufacturer || '';
                document.getElementById('drone-type').value = drone.type;
                document.getElementById('drone-maxTakeoffWeight').value = drone.maxTakeoffWeight || '';
                document.getElementById('drone-maxFlightTime').value = drone.maxFlightTime || '';
                document.getElementById('drone-releaseYear').value = drone.releaseYear || '';
                document.getElementById('drone-description').value = drone.description || '';
                document.getElementById('drone-maxRange').value = drone.maxRange || '';
                document.getElementById('drone-payloadCapacity').value = drone.payloadCapacity || '';
                document.getElementById('drone-sensorType').value = drone.sensorType || '';
                document.getElementById('drone-ipRating').value = drone.ipRating || '';
            });
    }

    document.getElementById('drone-modal').style.display = 'block';
}

// Закрытие модального окна
function closeModal() {
    document.getElementById('drone-modal').style.display = 'none';
    editingId = null;
}

// --- ОТПРАВКА ФОРМЫ (создание/обновление) ---
// Собирает данные из полей и отправляет POST или PUT запрос
document.getElementById('drone-form').onsubmit = async (e) => {
    e.preventDefault();

    const drone = {
        name: document.getElementById('drone-name').value,
        manufacturer: document.getElementById('drone-manufacturer').value,
        type: document.getElementById('drone-type').value,
        maxTakeoffWeight: parseFloat(document.getElementById('drone-maxTakeoffWeight').value),
        maxFlightTime: parseInt(document.getElementById('drone-maxFlightTime').value),
        releaseYear: parseInt(document.getElementById('drone-releaseYear').value) || null,
        description: document.getElementById('drone-description').value,
        maxRange: parseFloat(document.getElementById('drone-maxRange').value) || null,
        payloadCapacity: parseFloat(document.getElementById('drone-payloadCapacity').value) || null,
        sensorType: document.getElementById('drone-sensorType').value,
        ipRating: document.getElementById('drone-ipRating').value
    };

    try {
        const url = editingId ? `/api/drones/${editingId}` : '/api/drones';
        const method = editingId ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(drone)
        });

        if (!response.ok) throw new Error('Ошибка сохранения');

        closeModal();
        loadDrones();  // Перезагружаем таблицу
    } catch (error) {
        showError('Ошибка: ' + error.message);
    }
};

// --- РЕДАКТИРОВАНИЕ ---
// Просто открывает модалку с id дрона
async function editDrone(id) {
    openModal(id);
}

// --- УДАЛЕНИЕ ---
// Спрашивает подтверждение и отправляет DELETE-запрос
async function deleteDrone(id) {
    if (!confirm('Удалить дрона?')) return;

    try {
        const response = await fetch(`/api/drones/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Ошибка удаления');
        loadDrones();
    } catch (error) {
        showError('Ошибка: ' + error.message);
    }
}

// --- ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ---
// Показ/скрытие блока с ошибкой
function showError(message) {
    document.getElementById('error-message').innerHTML = `<div class="error">${message}</div>`;
}

function hideError() {
    document.getElementById('error-message').innerHTML = '';
}

// Закрытие модалки при клике вне её области
window.onclick = (event) => {
    const modal = document.getElementById('drone-modal');
    if (event.target === modal) closeModal();
};