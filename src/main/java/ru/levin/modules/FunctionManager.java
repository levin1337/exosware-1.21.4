package ru.levin.modules;

import ru.levin.modules.combat.*;
import ru.levin.modules.misc.*;
import ru.levin.modules.misc.ClientSounds;
import ru.levin.modules.player.GodMode;
import ru.levin.modules.misc.NoCommands;
import ru.levin.modules.misc.Optimizer;
import ru.levin.modules.movement.*;
import ru.levin.modules.player.*;
import ru.levin.modules.render.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FunctionManager {

    public static final List<Function> functions = new CopyOnWriteArrayList<>();
    public final ClickGUI clickGUI;
    public final Optimizer optimizer;
    public final ClientSounds clientSounds;
    public final ElytraTarget elytraTarget;
    public final SuperFirework superFirework;
    public final AttackAura attackAura;
    public final CrystalAura crystalAura;
    public final NoFriendDamage noFriendDamage;
    public final HitBox xbox;
    public final NoCommands noCommands;
    public final SwingAnimations swingAnimations;
    public final ViewModel viewModel;
    public final NoPush noPush;
    public final FreeCamera freeCamera;
    public final HUD hud;
    public final NoRender noRender;
    public final NameProtect nameProtect;
    public final NoInteract noInteract;
    public final ItemScroller itemScroller;
    public final NoSlow noSlow;
    public final LittleSnickers littleSnickers;
    public final UnHook unHook;
    public final FullBright fullBright;
    public final ItemPhysic itemPhysic;
    public final AutoPotion autoPotion;
    public final GuiWalk guiWalk;
    public final ExtraTab extraTab;
    public final FreeLook freeLook;
    public final AntiBot antiBot;
    public final AutoExplosion autoExplosion;
    public final Arrows arrows;
    public final CustomCoolDown customCoolDown;
    public final Blink blink;
    public final NameTags nameTags;
    public final BlockESP blockESP;
    public final ChestStealer chestStealer;
    public final AspectRatio aspectRatio;
    public final World customWorld;
    public final NoRayTrace noRayTrace;
    public final IRC irc;
    public final ClickAction clickAction;
    public final Phase phase;
    public final TargetStrafe targetStrafe;
    public final Globals globals;
    public final AutoSprint autoSprint;
    public final Speed speed;
    public final CrossHair crossHair;
    public final SelfTrap selfTrap;
    public final AutoTotem autoTotem;
    public final BlockHighLight blockHighLight;
    //public final KTLeave ktLeave;
    //public final MaceExploit maceExploit;
    public FunctionManager() {
        functions.addAll(Arrays.asList(
                //Combat
                new Criticals(),
                targetStrafe = new TargetStrafe(),
                noFriendDamage = new NoFriendDamage(),
                autoExplosion = new AutoExplosion(),
                new AttackExtend(),
                attackAura = new AttackAura(),
                crystalAura = new CrystalAura(),
                selfTrap = new SelfTrap(),
                autoPotion = new AutoPotion(),
                autoTotem = new AutoTotem(),
                new AutoSwap(),
                new SuperBow(),
                xbox = new HitBox(),
                antiBot = new AntiBot(),
                new Velocity(),
                //Misc
                unHook = new UnHook(),
                optimizer = new Optimizer(),
                clientSounds = new ClientSounds(),
                new DeathCoords(),
                new ServerRPSpoff(),
                new Xray(),
                new ElytraHelper(),
                new FTHelper(),
                new HWHelper(),
                new RWHelper(),
                new AutoDuel(),
                new AutoDuelBot(),
                new DiscordRCP(),
                globals = new Globals(),
                irc = new IRC(),
                nameProtect = new NameProtect(),
                noCommands = new NoCommands(),
                //Movement
                blink = new Blink(),
                phase = new Phase(),
                autoSprint = new AutoSprint(),
                new HighJump(),
                new Flight(),
                elytraTarget = new ElytraTarget(),
                new ElytraRecast(),
                new ElytraMotion(),
                superFirework = new SuperFirework(),
                freeLook = new FreeLook(),
                speed = new Speed(),
                new Strafe(),
                new Spider(),
                new AirStuck(),
                noSlow = new NoSlow(),
                new Timer(),
                new NoWeb(),
                //Player
                guiWalk = new GuiWalk(),
                new NoDelay(),
                new AutoLeave(),
                new AutoMessage(),
                clickAction = new ClickAction(),
                itemScroller = new ItemScroller(),
                new ItemFixSwap(),
                new PerfectTime(),
                noRayTrace = new NoRayTrace(),
                noPush = new NoPush(),
                new AutoRespawn(),
                new AutoTool(),
                freeCamera = new FreeCamera(),
                customCoolDown = new CustomCoolDown(),
                new MiddleClickFriend(),
                new MiddleClickPearl(),
                noInteract = new NoInteract(),
                chestStealer = new ChestStealer(),
                new EnderChestExploit(),
                new InvseeExploit(),
                new RegionExploit(),
      //          maceExploit = new MaceExploit(),
           //     new GodMode(),
                //Render
                clickGUI = new ClickGUI(),
                hud = new HUD(),
                swingAnimations = new SwingAnimations(),
                viewModel = new ViewModel(),
                aspectRatio = new AspectRatio(),
                crossHair = new CrossHair(),
                fullBright = new FullBright(),
           //     new ShulkerPreview(),
                customWorld = new World(),
                noRender = new NoRender(),
                blockESP = new BlockESP(),
                itemPhysic = new ItemPhysic(),
                extraTab = new ExtraTab(),
                arrows = new Arrows(),
                new ESP(),
                nameTags = new NameTags(),
                new Prediction(),
                blockHighLight = new BlockHighLight(),
                new AutoAccept(),
                new JumpCircles(),
                new Breadcrumbs(),
                new Trails(),
                new Particles(),
                new TargetESP(),
                new TPLoot(),
           //     ktLeave = new KTLeave(),
                littleSnickers = new LittleSnickers()
        ));
    }



    public List<Function> getFunctions() {
        return functions;
    }

    public List<Function> getFunctions(Type category) {
        List<Function> functions = new ArrayList<>();
        for (Function function : getFunctions()) {
            if (function.getCategory() == category) {
                functions.add(function);
            }

        }
        return functions;
    }

    public static Function get(String name) {
        for (Function function : functions) {
            if (function != null && function.name.equalsIgnoreCase(name)) {
                return function;
            }
        }
        return null;
    }
}
