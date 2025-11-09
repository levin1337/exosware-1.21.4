package ru.levin.manager;

import ru.levin.manager.accountManager.AccountManager;
import ru.levin.manager.commandManager.CommandManager;
import ru.levin.manager.configManager.ConfigManager;
import ru.levin.manager.dragManager.DragManager;
import ru.levin.manager.friendManager.FriendManager;
import ru.levin.manager.ircManager.IrcManager;
import ru.levin.manager.macroManager.MacroManager;
import ru.levin.manager.modulesManager.ChestStealerManager;
import ru.levin.manager.notificationManager.NotificationManager;
import ru.levin.manager.proxyManager.ProxyManager;
import ru.levin.manager.staffManager.StaffManager;
import ru.levin.manager.themeManager.StyleManager;
import ru.levin.modules.FunctionManager;
import ru.levin.modules.combat.rotation.RotationController;
import ru.levin.protect.UserProfile;
import ru.levin.manager.fontManager.FontUtils;

public class Manager {
    public static final RotationController ROTATION = RotationController.get();
    public static UserProfile USER_PROFILE;
    public static FunctionManager FUNCTION_MANAGER;
    public static StyleManager STYLE_MANAGER;
    public static NotificationManager NOTIFICATION_MANAGER;
    public static FriendManager FRIEND_MANAGER;
    public static ConfigManager CONFIG_MANAGER;
    public static MacroManager MACROS_MANAGER;
    public static StaffManager STAFF_MANAGER;
    public static CommandManager COMMAND_MANAGER;
    public static DragManager DRAG_MANAGER;
    public static SyncManager SYNC_MANAGER;
    public static FontUtils FONT_MANAGER;
    public static AccountManager ACCOUNT_MANAGER;
    public static ChestStealerManager CHESTSTEALER_MANAGER;
    public static IrcManager IRC_MANAGER;
    public static ProxyManager PROXY_MANAGER;
}
