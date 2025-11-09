package ru.levin;

import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import ru.levin.manager.*;
import ru.levin.manager.accountManager.AccountManager;
import ru.levin.manager.ircManager.IrcManager;
import ru.levin.manager.modulesManager.ChestStealerManager;
import ru.levin.manager.proxyManager.ProxyManager;
import ru.levin.manager.themeManager.StyleManager;
import ru.levin.protect.NativeHelper;
import ru.levin.modules.setting.BindBooleanSetting;
import ru.levin.modules.setting.Setting;
import ru.levin.events.Event;
import ru.levin.events.impl.input.EventKey;
import ru.levin.manager.commandManager.CommandManager;
import ru.levin.manager.configManager.ConfigManager;
import ru.levin.manager.dragManager.DragManager;
import ru.levin.manager.dragManager.Dragging;
import ru.levin.manager.friendManager.FriendManager;
import ru.levin.manager.macroManager.MacroManager;
import ru.levin.manager.notificationManager.NotificationManager;
import ru.levin.manager.staffManager.StaffManager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionManager;
import ru.levin.modules.misc.UnHook;
import ru.levin.screens.dropdown.ClickGUI;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.color.ColorUtil;
import ru.levin.util.player.AudioUtil;
import ru.levin.util.render.providers.ResourceProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.Objects;

@SuppressWarnings("All")
public final class ExosWare implements ModInitializer {
	private static ExosWare instance;
	private final File directory;
	private final File directoryAddon;
	public final String name = "ExosWare client";
	@Getter
	boolean initialized;

	public static ExosWare getInstance() {
		return instance;
	}

	public ExosWare() {
		instance = this;
		this.directory = new File(Objects.requireNonNull(MinecraftClient.getInstance().runDirectory), "files");
		this.directoryAddon = new File(Objects.requireNonNull(MinecraftClient.getInstance().runDirectory), "files/modules");
	}

	private void setupProtection() {
		NativeHelper.setProfile();
	}

	@Override
	public void onInitialize() {
		setupProtection();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				shutDown();
			} catch (Exception ignored) {}
		}));
	}

	public void init() {
		ensureDirectoryExists();
		try {
			Manager.SYNC_MANAGER = new SyncManager();
			Manager.FUNCTION_MANAGER = new FunctionManager();
			Manager.STYLE_MANAGER = new StyleManager();
			Manager.STYLE_MANAGER.init();
			Manager.ACCOUNT_MANAGER = new AccountManager();
			Manager.ACCOUNT_MANAGER.init();
			Manager.FONT_MANAGER = new FontUtils();
			Manager.FONT_MANAGER.init();
			Manager.COMMAND_MANAGER = new CommandManager();
			Manager.DRAG_MANAGER = new DragManager();
			Manager.DRAG_MANAGER.init();
			Manager.MACROS_MANAGER = new MacroManager();
			Manager.MACROS_MANAGER.init();
			Manager.FRIEND_MANAGER = new FriendManager();
			Manager.FRIEND_MANAGER.init();
			Manager.STAFF_MANAGER = new StaffManager();
			Manager.STAFF_MANAGER.init();
			Manager.NOTIFICATION_MANAGER = new NotificationManager();
			Manager.CHESTSTEALER_MANAGER = new ChestStealerManager();
			Manager.PROXY_MANAGER = new ProxyManager();
			Manager.PROXY_MANAGER.init();

			Manager.IRC_MANAGER = new IrcManager();
			Manager.IRC_MANAGER.connect(Manager.USER_PROFILE.getName());

			Manager.CONFIG_MANAGER = new ConfigManager();
			Manager.CONFIG_MANAGER.init();

			ColorUtil.loadImage(ResourceProvider.color_image);

			if (Manager.FUNCTION_MANAGER.clientSounds.check.get("Вход в клиент")) {
				AudioUtil.playSound("join.wav");
			}
			initialized = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void keyPress(int key) {
		int processedKey = key >= 0 ? key : -(100 + key + 2);
		Event.call(new EventKey(processedKey));

		if (key == Manager.FUNCTION_MANAGER.unHook.unHookKey.getKey() && ClientManager.legitMode) {
			UnHook.functionsToBack.forEach(function -> function.setState(true));
			File folder = new File("C:\\ExosWare");
			if (folder.exists()) {
				try {
					Path folderPathObj = folder.toPath();
					DosFileAttributeView attributes = Files.getFileAttributeView(folderPathObj, DosFileAttributeView.class);
					attributes.setHidden(false);
				} catch (IOException ignored) {
				}
			}
			UnHook.functionsToBack.clear();
			ClientManager.legitMode = false;
		}

		if (!ClientManager.legitMode) {
			for (Function module : Manager.FUNCTION_MANAGER.getFunctions()) {
				if (module.bind == processedKey) {
					module.toggle();
				}
				for (Setting setting : module.getSettings()) {
					if (setting instanceof BindBooleanSetting bindSetting) {
						bindSetting.onKeyPress(key, true);
					}
				}
			}

			if (key == Manager.FUNCTION_MANAGER.clickGUI.getBindCode()) {
				MinecraftClient.getInstance().setScreen(new ClickGUI());
			}
			if (Manager.MACROS_MANAGER != null) {
				Manager.MACROS_MANAGER.onKeyPressed(key);
			}
		}
	}

	public void shutDown() {
		Manager.DRAG_MANAGER.save();
		Manager.ACCOUNT_MANAGER.saveAccounts();
		Manager.ACCOUNT_MANAGER.saveLastAlt();
		Manager.CONFIG_MANAGER.saveConfiguration("autocfg");
		Manager.IRC_MANAGER.shutdown();
		Manager.FUNCTION_MANAGER.globals.clear();
		System.out.println("[-] Client shutdown");
	}
	public static void openURL(String url) {
		try {
			String os = System.getProperty("os.name").toLowerCase();

			if (os.contains("win")) {
				Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
			} else if (os.contains("mac")) {
				Runtime.getRuntime().exec(new String[]{"open", url});
			} else {
				Runtime.getRuntime().exec(new String[]{"xdg-open", url});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Dragging createDrag(Function function, String name, float x, float y) {
		DragManager.draggables.put(name, new Dragging(function, name, x, y));
		return DragManager.draggables.get(name);
	}
	private void ensureDirectoryExists() {
		if (!directory.exists() && !directory.mkdirs()) {
			System.err.println("Failed to create directory: " + directory.getAbsolutePath());
		}
		if (!directoryAddon.exists() && !directoryAddon.mkdirs()) {
			System.err.println("Failed to create directory: " + directoryAddon.getAbsolutePath());
		}
	}
}