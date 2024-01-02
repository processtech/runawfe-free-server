package ru.runa.common.web;

public final class MessagesCommon {
    // Forward menu (left main menu items)

    // Forward menu (left main menu items) -> User tasks.
    public static final StrutsMessage MAIN_MENU_ITEM_TASKS = new StrutsMessage("manage_tasks");
    // Forward menu (left main menu items) -> All tasks.
    public static final StrutsMessage MAIN_MENU_ITEM_OBSERVABLE_TASKS = new StrutsMessage("manage_observable_tasks");
    // Forward menu (left main menu items) -> Process definitions.
    public static final StrutsMessage MAIN_MENU_ITEM_DEFINITIONS = new StrutsMessage("manage_definitions");
    // Forward menu (left main menu items) -> Started processes.
    public static final StrutsMessage MAIN_MENU_ITEM_PROCESSES = new StrutsMessage("manage_processes");
    // Forward menu (left main menu items) -> Archived processes.
    public static final StrutsMessage MAIN_MENU_ITEM_ARCHIVED_PROCESSES = new StrutsMessage("manage_archived_processes");
    // Forward menu (left main menu items) -> Executors.
    public static final StrutsMessage MAIN_MENU_ITEM_EXECUTORS = new StrutsMessage("manage_executors");
    // Forward menu (left main menu items) -> Reports.
    public static final StrutsMessage MAIN_MENU_ITEM_REPORTS = new StrutsMessage("manage_reports");
    // Forward menu (left main menu items) -> Relations.
    public static final StrutsMessage MAIN_MENU_ITEM_RELATIONS = new StrutsMessage("manage_relations");
    // Forward menu (left main menu items) -> Bot station.
    public static final StrutsMessage MAIN_MENU_ITEM_BOT_STATION = new StrutsMessage("configure_bot_station");
    // Forward menu (left main menu items) -> Data sources.
    public static final StrutsMessage MAIN_MENU_ITEM_DATA_SOURCES = new StrutsMessage("manage_data_sources");
    // Forward menu (left main menu items) -> System.
    public static final StrutsMessage MAIN_MENU_ITEM_SYSTEM = new StrutsMessage("manage_system");
    // Forward menu (left main menu items) -> Errors.
    public static final StrutsMessage MAIN_MENU_ITEM_ERRORS = new StrutsMessage("manage_errors");
    // Forward menu (left main menu items) -> Frozen processes.
    public static final StrutsMessage MAIN_MENU_ITEM_FROZEN_PROCESSES = new StrutsMessage("manage_frozen_processes");
    // Forward menu (left main menu items) -> Settings.
    public static final StrutsMessage MAIN_MENU_ITEM_SETTINGS = new StrutsMessage("manage_settings");
    // Forward menu (left main menu items) -> Logs.
    public static final StrutsMessage MAIN_MENU_ITEM_LOGS = new StrutsMessage("view_logs");
    // Forward menu (left main menu items) -> Internal storage.
    public static final StrutsMessage MAIN_MENU_ITEM_INTERNAL_STORAGE = new StrutsMessage("view_internal_storage");
    // Forward menu (left main menu items) -> Send signal.
    public static final StrutsMessage MAIN_MENU_ITEM_SEND_SIGNAL = new StrutsMessage("send_process_signal");
    // Forward menu (left main menu items) -> Chats.
    public static final StrutsMessage MAIN_MENU_ITEM_CHATS = new StrutsMessage("chat_rooms");

    // Common buttons

    // Add
    public static final StrutsMessage BUTTON_ADD = new StrutsMessage("button.add");
    // Apply
    public static final StrutsMessage BUTTON_APPLY = new StrutsMessage("button.apply");
    // Cancel
    public static final StrutsMessage BUTTON_CANCEL = new StrutsMessage("button.cancel");
    // Restore
    public static final StrutsMessage BUTTON_RESTORE = new StrutsMessage("button.restore");
    // Remove
    public static final StrutsMessage BUTTON_REMOVE = new StrutsMessage("button.remove");
    // Save
    public static final StrutsMessage BUTTON_SAVE = new StrutsMessage("button.save");
    // Save as
    public static final StrutsMessage BUTTON_SAVE_AS = new StrutsMessage("button.save_as");
    // Create
    public static final StrutsMessage BUTTON_CREATE = new StrutsMessage("button.create");
    public static final StrutsMessage BUTTON_CHANGE = new StrutsMessage("button.change");
    // Update
    public static final StrutsMessage BUTTON_UPDATE = new StrutsMessage("button.update");

    // Common messages

    // "Properties" message, to show object property.
    public static final StrutsMessage LABEL_PROPERTIES = new StrutsMessage("label.properties");

    // Logout link
    public static final StrutsMessage LOGOUT = new StrutsMessage("button.logout");

    // Message to requesting password
    public static final StrutsMessage PASSWORD = new StrutsMessage("label.password");
    // Message to requesting password confirmation
    public static final StrutsMessage PASSWORD_CONFIRM = new StrutsMessage("label.password_confirm");

    // No type selected.
    public static final StrutsMessage NO_TYPE_SELECTED = new StrutsMessage("batch_presentation.process.no_type");

    // Message to group objects by id.
    public static final StrutsMessage LABEL_GROUP_BY_ID = new StrutsMessage("batch_presentation.process.group_by_id");

    public static final StrutsMessage TITLE_PERMISSION_OWNERS = new StrutsMessage("title.permission_owners");
    public static final StrutsMessage TITLE_EXECUTORS_PERMISSIONS = new StrutsMessage("title.executors_permissions");

    public static final StrutsMessage HEADER_PARAMETER_NAME = new StrutsMessage("header.parameter.name");
    public static final StrutsMessage HEADER_PARAMETER_VALUE = new StrutsMessage("header.parameter.value");
}
