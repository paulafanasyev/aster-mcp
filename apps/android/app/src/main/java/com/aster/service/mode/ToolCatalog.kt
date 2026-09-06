package com.aster.service.mode

/**
 * Static catalog mapping action names to meaningful categories and descriptions.
 * Used by both IPC and MCP modes for dashboard display.
 */
object ToolCatalog {

    data class ToolEntry(
        val displayName: String,
        val description: String,
        val category: String
    )

    /** Ordered list of categories for display (controls section ordering). */
    val categoryOrder = listOf(
        "Управление экраном",
        "Устройство",
        "Файлы",
        "Камера",
        "Связь",
        "Уведомления",
        "Мультимедиа",
        "Хранилище",
        "Приложения",
        "Система",
        "Наложения",
        "Будильники"
    )

    private val catalog = mapOf(
        // -- Screen Control --
        "observe" to ToolEntry(
            "Наблюдать за экраном",
            "Индексированное представление элементов текущего экрана со стабильными ссылками; " +
                "при разреженном дереве специальных возможностей используется OCR на устройстве",
            "Управление экраном"
        ),
        "get_screen_hierarchy" to ToolEntry(
            "Иерархия экрана",
            "Читать дерево специальных возможностей интерфейса",
            "Управление экраном"
        ),
        "take_screenshot" to ToolEntry(
            "Снимок экрана",
            "Сохранить экран как JPEG; при необходимости добавить нумерованные области",
            "Управление экраном"
        ),
        "input_gesture" to ToolEntry(
            "Жест",
            "Нажатие, свайп или долгое нажатие на экране",
            "Управление экраном"
        ),
        "input_text" to ToolEntry(
            "Ввести текст",
            "Ввести текст в активное поле",
            "Управление экраном"
        ),
        "find_element" to ToolEntry(
            "Найти элемент",
            "Искать элементы интерфейса по тексту",
            "Управление экраном"
        ),
        "click_by_text" to ToolEntry(
            "Нажать по тексту",
            "Найти и нажать элемент по видимому тексту",
            "Управление экраном"
        ),
        "click_by_view_id" to ToolEntry(
            "Нажать по ID",
            "Найти и нажать элемент по ID представления",
            "Управление экраном"
        ),
        "scroll" to ToolEntry("Прокрутка", "Прокрутка в любом направлении", "Управление экраном"),
        "tap" to ToolEntry("Нажатие", "Нажать элемент по ссылке или координатам", "Управление экраном"),
        "long_press" to ToolEntry(
            "Долгое нажатие",
            "Долго нажать элемент по ссылке или координатам",
            "Управление экраном"
        ),
        "set_text" to ToolEntry(
            "Установить текст",
            "Ввести текст в поле по ссылке (заменить или добавить)",
            "Управление экраном"
        ),
        "set_toggle" to ToolEntry(
            "Установить переключатель",
            "Включить или выключить переключатель или флажок по ссылке",
            "Управление экраном"
        ),
        "perform" to ToolEntry(
            "Выполнить действие",
            "Выполнить действие специальных возможностей над элементом по ссылке",
            "Управление экраном"
        ),
        "press_key" to ToolEntry(
            "Нажать клавишу",
            "Нажать аппаратную или IME-клавишу (Enter, Назад, Tab, стрелки…)",
            "Управление экраном"
        ),
        "wait_for_idle" to ToolEntry(
            "Ждать бездействия",
            "Ждать, пока экран перестанет изменяться",
            "Управление экраном"
        ),
        "wait_for" to ToolEntry(
            "Ждать элемент",
            "Ждать появления или исчезновения элемента",
            "Управление экраном"
        ),
        "global_action" to ToolEntry(
            "Системное действие",
            "Главный экран, назад, последние приложения и другие системные действия",
            "Управление экраном"
        ),

        // -- Device --
        "get_device_info" to ToolEntry(
            "Сведения об устройстве",
            "Модель, ОС, ОЗУ, хранилище и сведения об оборудовании",
            "Устройство"
        ),
        "get_battery" to ToolEntry(
            "Батарея",
            "Уровень батареи, зарядка и состояние аккумулятора",
            "Устройство"
        ),
        "get_location" to ToolEntry(
            "Местоположение",
            "Текущие координаты GPS и данные о местоположении",
            "Устройство"
        ),

        // -- Files --
        "list_files" to ToolEntry("Список файлов", "Просматривать файлы и папки с метаданными", "Файлы"),
        "read_file" to ToolEntry("Прочитать файл", "Читать текстовое или двоичное содержимое файла", "Файлы"),
        "write_file" to ToolEntry("Записать файл", "Создавать или перезаписывать файлы на устройстве", "Файлы"),
        "delete_file" to ToolEntry("Удалить файл", "Удалять файлы и каталоги", "Файлы"),
        "files.read" to ToolEntry("Прочитать файл хоста", "Читать файл из папки, разрешённой разработчиком", "Файлы"),
        "files.list" to ToolEntry("Список папки хоста", "Просматривать папку, разрешённую разработчиком для приложения", "Файлы"),

        // -- Camera --
        "take_photo" to ToolEntry(
            "Сделать фото",
            "Сделать фото передней или задней камерой",
            "Камера"
        ),
        "record_video" to ToolEntry(
            "Записать видео",
            "Записать короткое видео камерой",
            "Камера"
        ),

        // -- Communication --
        "send_sms" to ToolEntry("Отправить SMS", "Отправлять текстовые сообщения", "Связь"),
        "read_sms" to ToolEntry("Прочитать SMS", "Читать входящие, отправленные или все сообщения", "Связь"),
        "count_sms" to ToolEntry("Посчитать SMS", "Считать сообщения за выбранный период", "Связь"),
        "make_call" to ToolEntry("Телефонный звонок", "Начать телефонный звонок", "Связь"),
        "make_call_with_voice" to ToolEntry(
            "Позвонить и сказать",
            "Позвонить и произнести текст после ответа",
            "Связь"
        ),
        "search_contacts" to ToolEntry(
            "Найти контакты",
            "Найти контакты по имени или номеру телефона",
            "Связь"
        ),

        // -- Notifications --
        "read_notifications" to ToolEntry(
            "Прочитать",
            "Получить активные и недавние уведомления",
            "Уведомления"
        ),
        "post_notification" to ToolEntry("Показать", "Показать локальное уведомление", "Уведомления"),
        "dismiss_notification" to ToolEntry(
            "Закрыть",
            "Скрыть выбранное уведомление",
            "Уведомления"
        ),
        "dismiss_all_notifications" to ToolEntry(
            "Закрыть все",
            "Очистить все активные уведомления",
            "Уведомления"
        ),

        // -- Media --
        "play_audio" to ToolEntry("Воспроизвести аудио", "Воспроизвести аудио из URL, файла или данных", "Мультимедиа"),
        "stop_audio" to ToolEntry("Остановить аудио", "Остановить текущее воспроизведение", "Мультимедиа"),
        "speak_tts" to ToolEntry("Синтез речи", "Произнести текст вслух через движок TTS", "Мультимедиа"),
        "vibrate" to ToolEntry("Вибрация", "Вибрировать по заданному шаблону", "Мультимедиа"),
        "get_now_playing" to ToolEntry(
            "Сейчас играет",
            "Получить текущий трек (название, исполнитель, приложение-источник) из ОС " +
                "медиасессий; используется доступ к уведомлениям без дополнительного разрешения",
            "Мультимедиа"
        ),

        // -- Storage --
        "analyze_storage" to ToolEntry(
            "Анализировать",
            "Разбивка использования диска по каталогам и типам",
            "Хранилище"
        ),
        "find_large_files" to ToolEntry(
            "Большие файлы",
            "Найти файлы больше заданного размера",
            "Хранилище"
        ),
        "index_media_metadata" to ToolEntry(
            "Индексировать медиа",
            "Индексировать фото и видео с EXIF и GPS",
            "Хранилище"
        ),
        "search_media" to ToolEntry(
            "Поиск медиа",
            "Искать по дате, местоположению, типу или камере",
            "Хранилище"
        ),

        // -- Apps --
        "list_packages" to ToolEntry("Установленные приложения", "Показать все приложения и версии", "Приложения"),
        "launch_intent" to ToolEntry("Запустить приложение", "Запускать приложения или пользовательские Android Intent", "Приложения"),

        // -- System --
        "screen_set_policy" to ToolEntry(
            "Синхронизировать политику приложения",
            "Получать список разрешений и запретов управления экраном по приложениям",
            "Система"
        ),
        "execute_shell" to ToolEntry("Команда оболочки", "Выполнять команды в песочнице приложения", "Система"),
        "get_clipboard" to ToolEntry("Получить буфер обмена", "Читать текущее содержимое буфера обмена", "Система"),
        "set_clipboard" to ToolEntry("Установить буфер обмена", "Копировать текст в буфер обмена", "Система"),
        "get_volume" to ToolEntry("Получить громкость", "Уровни громкости всех аудиопотоков", "Система"),
        "set_volume" to ToolEntry("Установить громкость", "Изменять громкость или отключать звук", "Система"),
        "show_toast" to ToolEntry("Всплывающее сообщение", "Показать короткое сообщение на экране", "Система"),

        // -- Overlays --
        "show_overlay" to ToolEntry(
            "Показать наложение",
            "Показать плавающее HTML-окно поверх экрана",
            "Наложения"
        ),
        "hide_overlay" to ToolEntry("Скрыть наложение", "Скрыть выбранное плавающее окно", "Наложения"),
        "hide_all_overlays" to ToolEntry("Скрыть все", "Убрать все активные наложения", "Наложения"),
        "list_overlays" to ToolEntry("Список наложений", "Показать ID активных наложений", "Наложения"),
        "companion_overlay_status" to ToolEntry(
            "Состояние лица помощника",
            "Можно ли отображать лицо поверх других приложений и запущено ли лицо помощника",
            "Наложения"
        ),
        "companion_overlay_show" to ToolEntry(
            "Показать лицо помощника",
            "Показать лицо помощника рядом с вырезом камеры",
            "Наложения"
        ),
        "companion_overlay_hide" to ToolEntry(
            "Скрыть лицо помощника",
            "Скрыть лицо помощника",
            "Наложения"
        ),
        "companion_overlay_recompute" to ToolEntry(
            "Переместить лицо помощника",
            "Заново рассчитать положение лица помощника вокруг выреза камеры",
            "Наложения"
        ),

        // -- Alarms --
        "get_alarms" to ToolEntry("Получить будильники", "Просмотреть запланированные будильники", "Будильники"),
        "set_alarm" to ToolEntry("Установить будильник", "Создать новый будильник", "Будильники"),
        "dismiss_alarm" to ToolEntry("Остановить будильник", "Остановить звонящий будильник", "Будильники"),
        "delete_alarm" to ToolEntry("Удалить будильник", "Удалить сохранённый будильник", "Будильники"),
    )

    /**
     * Get enriched ToolInfo for a given action name.
     * Falls back to auto-generated info if action isn't in the catalog.
     */
    fun getToolInfo(action: String): ToolInfo {
        val entry = catalog[action]
        return if (entry != null) {
            ToolInfo(
                name = action,
                displayName = entry.displayName,
                description = entry.description,
                category = entry.category
            )
        } else {
            // Fallback: derive from action name
            val displayName = action.replace("_", " ").replaceFirstChar { it.uppercase() }
            ToolInfo(
                name = action,
                displayName = displayName,
                description = action,
                category = "Другое"
            )
        }
    }

    /**
     * Convert a list of action names to sorted, categorized ToolInfo list.
     */
    fun resolve(actions: Collection<String>): List<ToolInfo> {
        return actions
            .map { getToolInfo(it) }
            .sortedWith(compareBy({
                categoryOrder.indexOf(it.category).let { i -> if (i == -1) 999 else i }
            }, { it.displayName }))
    }
}
