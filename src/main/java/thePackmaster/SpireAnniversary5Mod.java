package thePackmaster;

import basemod.AutoAdd;
import basemod.BaseMod;
import basemod.abstracts.CustomSavable;
import basemod.helpers.CardBorderGlowManager;
import basemod.helpers.RelicType;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.mod.stslib.Keyword;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.DiscardToHandAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardHelper;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.*;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.ArtifactPower;
import com.megacrit.cardcrawl.powers.watcher.VigorPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import javassist.CtClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thePackmaster.cardmodifiers.transmutationpack.dynamicdynamic.DynamicDynamicVariableManager;
import thePackmaster.cards.AbstractPackmasterCard;
import thePackmaster.cards.batterpack.UltimateHomerun;
import thePackmaster.cards.bitingcoldpack.GrowingAffliction;
import thePackmaster.cards.cardvars.SecondDamage;
import thePackmaster.cards.cardvars.SecondMagicNumber;
import thePackmaster.cards.ringofpainpack.Slime;
import thePackmaster.orbs.summonspack.Louse;
import thePackmaster.orbs.summonspack.Panda;
import thePackmaster.packs.*;
import thePackmaster.patches.MainMenuUIPatch;
import thePackmaster.patches.marisapack.AmplifyPatches;
import thePackmaster.patches.psychicpack.DeepDreamPatch;
import thePackmaster.patches.psychicpack.occult.OccultFields;
import thePackmaster.patches.psychicpack.occult.OccultPatch;
import thePackmaster.potions.clawpack.AttackPotionButClaw;
import thePackmaster.potions.clawpack.ClawPowerPotion;
import thePackmaster.potions.clawpack.DrawClawsPotion;
import thePackmaster.potions.clawpack.GenerateClawsPotion;
import thePackmaster.powers.bitingcoldpack.FrostbitePower;
import thePackmaster.powers.bitingcoldpack.GlaciatePower;
import thePackmaster.relics.AbstractPackmasterRelic;
import thePackmaster.screens.PackSetupScreen;
import thePackmaster.ui.CurrentRunCardsTopPanelItem;
import thePackmaster.ui.PackFilterMenu;
import thePackmaster.util.cardvars.HoardVar;
import thePackmaster.vfx.distortionpack.ImproveEffect;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static thePackmaster.patches.MainMenuUIPatch.CHOICE;
import static thePackmaster.patches.MainMenuUIPatch.RANDOM;
import static thePackmaster.util.Wiz.*;

@SuppressWarnings({"unused", "WeakerAccess"})
@SpireInitializer
public class SpireAnniversary5Mod implements
        EditCardsSubscriber,
        EditRelicsSubscriber,
        EditStringsSubscriber,
        EditKeywordsSubscriber,
        EditCharactersSubscriber,
        PostInitializeSubscriber,
        PostUpdateSubscriber,
        OnStartBattleSubscriber,
        AddAudioSubscriber,
        PostBattleSubscriber,
        PostPowerApplySubscriber,
        StartGameSubscriber,
        PostExhaustSubscriber,
        CustomSavable<ArrayList<String>> {
    private static final Logger logger = LogManager.getLogger("Packmaster");

    public static HashMap<String, String> cardParentMap = new HashMap<>(); //Is filled in initializePack from AbstractCardPack. <cardID, packID>
    public static HashMap<Class<? extends AbstractCard>, String> cardClassParentMap = new HashMap<>(); //Is filled in initializePack from AbstractCardPack. <card Class, packID>
    public static ArrayList<AbstractCardPack> allPacks = new ArrayList<>();
    public static ArrayList<AbstractCardPack> unfilteredAllPacks = new ArrayList<>();
    public static HashMap<String, AbstractCardPack> packsByID;
    public static Set<String> packExclusivePotions = new HashSet<>();
    public static ArrayList<AbstractCardPack> currentPoolPacks = new ArrayList<>();
    public static CardGroup packsToDisplay;
    public static Settings.GameLanguage[] SupportedLanguages = {
            Settings.GameLanguage.ENG,
    };

    public static Color characterColor = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1); // This should be changed eventually

    public static SpireAnniversary5Mod thismod;
    public static SpireConfig modConfig = null;

    public static boolean doPackSetup = false;
    public static String lastCardsPackID = null;
    public static boolean openedStarterScreen = false;
    public static boolean skipDefaultCardRewards = false;
    public static int PACKS_PER_RUN = 7;
    public static CurrentRunCardsTopPanelItem currentRunCardsTopPanelItem;

    public static final String modID = "anniv5";
    public static final String SHOULDER1 = modID + "Resources/images/char/mainChar/shoulder.png";
    public static final String SHOULDER2 = modID + "Resources/images/char/mainChar/shoulder2.png";
    public static final String CORPSE = modID + "Resources/images/char/mainChar/corpse.png";
    public static final String SKELETON_JSON = modID + "Resources/images/char/mainChar/PackmasterAnim.json";
    public static final String SKELETON_ATLAS = modID + "Resources/images/char/mainChar/PackmasterAnim.atlas";
    private static final String ATTACK_S_ART = modID + "Resources/images/512/attack.png";
    private static final String SKILL_S_ART = modID + "Resources/images/512/skill.png";
    private static final String POWER_S_ART = modID + "Resources/images/512/power.png";
    private static final String CARD_ENERGY_S = modID + "Resources/images/512/energy.png";
    private static final String TEXT_ENERGY = modID + "Resources/images/512/text_energy.png";
    private static final String ATTACK_L_ART = modID + "Resources/images/1024/attack.png";
    private static final String SKILL_L_ART = modID + "Resources/images/1024/skill.png";
    private static final String POWER_L_ART = modID + "Resources/images/1024/power.png";
    private static final String CARD_ENERGY_L = modID + "Resources/images/1024/energy.png";
    private static final String CHARSELECT_BUTTON = modID + "Resources/images/charSelect/charButton.png";
    private static final String CHARSELECT_PORTRAIT = modID + "Resources/images/charSelect/charBG.png";

    public static final String BEES_KEY = makeID("SwarmOfBees");
    private static final String BEES_OGG = makePath("audio/summonspack/SwarmOfBees.ogg");
    public static final String ELEPHANT_KEY = makeID("elephant");
    private static final String ELEPHANT_OGG = makePath("audio/summonspack/Elephant.ogg");
    public static final String PEW_KEY = makeID("Pew");
    private static final String PEW_OGG = makePath("audio/summonspack/Pew.ogg");
    public static final String EVIL_KEY = makeID("Evil");
    private static final String EVIL_OGG = makePath("audio/summonspack/Evil.ogg");
    public static final String PANDA_KEY = makeID("Panda");
    private static final String PANDA_OGG = makePath("audio/summonspack/Panda.ogg");
    public static final String PORCUPINE_KEY = makeID("Porcupine");
    private static final String PORCUPINE_OGG = makePath("audio/summonspack/Porcupine.ogg");
    public static final String TRANSMUTATION_KEY = makeID("Transmutation");
    private static final String TRANSMUTATION_OGG = makePath("audio/transmutationpack/Transmutation.ogg");
    public static final String WATER_IMPACT_1_KEY = makeID("WaterImpactOne");
    private static final String WATER_IMPACT_1_OGG = makePath("audio/transmutationpack/WaterImpactOne.ogg");
    public static final String WATER_IMPACT_2_KEY = makeID("WaterImpactTwo");
    private static final String WATER_IMPACT_2_OGG = makePath("audio/transmutationpack/WaterImpactTwo.ogg");
    public static final String WATER_IMPACT_3_KEY = makeID("WaterImpactThree");
    private static final String WATER_IMPACT_3_OGG = makePath("audio/transmutationpack/WaterImpactThree.ogg");
    public static final String ICE_IMPACT_1_KEY = makeID("IceImpactOne");
    private static final String ICE_IMPACT_1_OGG = makePath("audio/transmutationpack/IceImpactOne.ogg");
    public static final String ICE_IMPACT_2_KEY = makeID("IceImpactTwo");
    private static final String ICE_IMPACT_2_OGG = makePath("audio/transmutationpack/IceImpactTwo.ogg");
    public static final String ICE_IMPACT_3_KEY = makeID("IceImpactThree");
    private static final String ICE_IMPACT_3_OGG = makePath("audio/transmutationpack/IceImpactThree.ogg");
    public static final String STEAM_IMPACT_1_KEY = makeID("SteamImpactOne");
    private static final String STEAM_IMPACT_1_OGG = makePath("audio/transmutationpack/SteamImpactOne.ogg");
    public static final String STEAM_IMPACT_2_KEY = makeID("SteamImpactTwo");
    private static final String STEAM_IMPACT_2_OGG = makePath("audio/transmutationpack/SteamImpactTwo.ogg");
    public static final String STEAM_IMPACT_3_KEY = makeID("StreamImpactThree");
    private static final String STEAM_IMPACT_3_OGG = makePath("audio/transmutationpack/SteamImpactThree.ogg");
    public static final String GUN1_KEY = makeID("Gun1");
    private static final String GUN1_OGG = makePath("audio/hermitpack/GUN1.ogg");
    public static final String GUN2_KEY = makeID("Gun2");
    private static final String GUN2_OGG = makePath("audio/hermitpack/GUN2.ogg");
    public static final String GUN3_KEY = makeID("Gun3");
    private static final String GUN3_OGG = makePath("audio/hermitpack/GUN3.ogg");

    public static final String EVIL_EFFECT_FILE = makePath("images/orbs/summonsPack/Evil.png");

    public static final ArrayList<Panda> pandaList = new ArrayList<>();
    public static final ArrayList<Louse> louseList = new ArrayList<>();

    public static boolean selectedCards = false;
    public static int combatExhausts = 0;

    public static int CLAW_SHARP_TRACKER = 0;

    public static String makeID(String idText) {
        return modID + ":" + idText;
    }

    @SpireEnum
    public static AbstractCard.CardTags ISCARDMODIFIED;

    @SpireEnum
    public static AbstractCard.CardTags MAGIC;

    @SpireEnum
    public static AbstractCard.CardTags CLAW;

    public SpireAnniversary5Mod() {
        BaseMod.subscribe(this);

        BaseMod.addColor(ThePackmaster.Enums.PACKMASTER_RAINBOW, characterColor, characterColor, characterColor,
                characterColor, characterColor, characterColor, characterColor,
                ATTACK_S_ART, SKILL_S_ART, POWER_S_ART, CARD_ENERGY_S,
                ATTACK_L_ART, SKILL_L_ART, POWER_L_ART,
                CARD_ENERGY_L, TEXT_ENERGY);
    }

    public static String makePath(String resourcePath) {
        return modID + "Resources/" + resourcePath;
    }

    public static String makeImagePath(String resourcePath) {
        return modID + "Resources/images/" + resourcePath;
    }

    public static String makeRelicPath(String resourcePath) {
        return modID + "Resources/images/relics/" + resourcePath;
    }

    public static String makePowerPath(String resourcePath) {
        return modID + "Resources/images/powers/" + resourcePath;
    }

    public static String makeCardPath(String resourcePath) {
        return modID + "Resources/images/cards/" + resourcePath;
    }

    public static String makeShaderPath(String resourcePath) {
        return modID + "Resources/shaders/" + resourcePath;
    }
    
    public static String makeOrbPath(String resourcePath) { return modID +"Resources/images/orbs/" + resourcePath; }

    public static void initialize() {
        thismod = new SpireAnniversary5Mod();

        try {
            Properties defaults = new Properties();
            defaults.put("PackmasterCustomDraftSelection", String.join(",", makeID("CoreSetPack"), RANDOM, RANDOM, RANDOM, RANDOM, CHOICE, CHOICE));
            modConfig = new SpireConfig(modID, "GeneralConfig", defaults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getSavedCDraftSelection() {
        if(modConfig == null) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(modConfig.getString("PackmasterCustomDraftSelection").split(",")));
    }
    public static void saveCDraftSelection(ArrayList<String> input) throws IOException {
        if(modConfig == null) return;
        modConfig.setString("PackmasterCustomDraftSelection", String.join(",", input));
        modConfig.save();
    }

    @Override
    public void receiveEditCharacters() {
        BaseMod.addCharacter(new ThePackmaster(ThePackmaster.characterStrings.NAMES[1], ThePackmaster.Enums.THE_PACKMASTER),
                CHARSELECT_BUTTON, CHARSELECT_PORTRAIT, ThePackmaster.Enums.THE_PACKMASTER);
    }

    @Override
    public void receiveEditRelics() {
        new AutoAdd(modID)
                .packageFilter(AbstractPackmasterRelic.class)
                .any(AbstractPackmasterRelic.class, (info, relic) -> {
                    if (relic.color == null) {
                        BaseMod.addRelic(relic, RelicType.SHARED);
                    } else {
                        BaseMod.addRelicToCustomPool(relic, relic.color);
                    }
                    if (!info.seen) {
                        UnlockTracker.markRelicAsSeen(relic.relicId);
                    }
                });
    }

    @Override
    public void receiveEditCards() {
        BaseMod.addDynamicVariable(new SecondMagicNumber());
        BaseMod.addDynamicVariable(new SecondDamage());
        BaseMod.addDynamicVariable(new HoardVar());
        new AutoAdd(modID)
                .packageFilter(AbstractPackmasterCard.class)
                .setDefaultSeen(true)
                .cards();

    }

    @Override
    public void receivePostInitialize() {
        declarePacks();
        logger.info("Full list of packs: " + unfilteredAllPacks.stream().map(pack -> pack.name).collect(Collectors.toList()));

        AmplifyPatches.receivePostInit();
        BaseMod.addCustomScreen(new PackSetupScreen());

        logger.info("Prepping dream hand");
        DeepDreamPatch.dreamHand = new DeepDreamPatch.DreamHand();

        logger.info("Checking playability annotations");
        OccultPatch.testPlayability();

        final Color occultGlow = CardHelper.getColor(88.0f, 26.0f, 150.0f);
        CardBorderGlowManager.addGlowInfo(new CardBorderGlowManager.GlowInfo() {
            @Override
            public boolean test(AbstractCard card) {
                return OccultFields.isOccult.get(card);
            }

            @Override
            public Color getColor(AbstractCard card) {
                return occultGlow.cpy();
            }

            @Override
            public String glowID() {
                return makeID("OccultGlow");
            }
        });

        currentRunCardsTopPanelItem = new CurrentRunCardsTopPanelItem();
        BaseMod.addSaveField("Anniversary5Mod", thismod);

        addPotions();
    }

    public static void addPotions() {
        BaseMod.addPotion(AttackPotionButClaw.class, Color.RED, Color.WHITE, Color.FIREBRICK, AttackPotionButClaw.POTION_ID, ThePackmaster.Enums.THE_PACKMASTER);
        BaseMod.addPotion(ClawPowerPotion.class, Color.RED, Color.WHITE, Color.FIREBRICK, ClawPowerPotion.POTION_ID, ThePackmaster.Enums.THE_PACKMASTER);
        BaseMod.addPotion(DrawClawsPotion.class, Color.RED, Color.WHITE, Color.FIREBRICK, DrawClawsPotion.POTION_ID, ThePackmaster.Enums.THE_PACKMASTER);
        BaseMod.addPotion(GenerateClawsPotion.class, Color.RED, Color.WHITE, Color.FIREBRICK, GenerateClawsPotion.POTION_ID, ThePackmaster.Enums.THE_PACKMASTER);

        if (Loader.isModLoaded("widepotions")) {
            Consumer<String> whitelist = getWidePotionsWhitelistMethod();
            whitelist.accept(AttackPotionButClaw.POTION_ID);
            whitelist.accept(ClawPowerPotion.POTION_ID);
            whitelist.accept(DrawClawsPotion.POTION_ID);
            whitelist.accept(GenerateClawsPotion.POTION_ID);
        }
    }

    private static Consumer<String> getWidePotionsWhitelistMethod() {
        // To avoid the need for a dependency of any kind, we call Wide Potions through reflection
        try {
            Method whitelistMethod = Class.forName("com.evacipated.cardcrawl.mod.widepotions.WidePotionsMod").getMethod("whitelistSimplePotion", String.class);
            return s -> {
                try {
                    whitelistMethod.invoke(null, s);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error trying to whitelist wide potion for " + s, e);
                }
            };
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not find method WidePotionsMod.whitelistSimplePotion", e);
        }
    }

    private String getLangString() {
        for (Settings.GameLanguage lang : SupportedLanguages) {
            if (lang.equals(Settings.language)) {
                return Settings.language.name().toLowerCase();
            }
        }
        return "eng";
    }

    @Override
    public void receiveEditStrings() {
        BaseMod.loadCustomStringsFile(CardStrings.class, modID + "Resources/localization/" + getLangString() + "/Cardstrings.json");
        BaseMod.loadCustomStringsFile(RelicStrings.class, modID + "Resources/localization/" + getLangString() + "/Relicstrings.json");
        BaseMod.loadCustomStringsFile(CharacterStrings.class, modID + "Resources/localization/" + getLangString() + "/Charstrings.json");
        BaseMod.loadCustomStringsFile(PowerStrings.class, modID + "Resources/localization/" + getLangString() + "/Powerstrings.json");
        BaseMod.loadCustomStringsFile(UIStrings.class, modID + "Resources/localization/" + getLangString() + "/UIstrings.json");
        BaseMod.loadCustomStringsFile(StanceStrings.class, modID + "Resources/localization/" + getLangString() + "/Stancestrings.json");
        BaseMod.loadCustomStringsFile(OrbStrings.class, modID + "Resources/localization/" + getLangString() + "/Orbstrings.json");

        loadPackStrings();
    }

    // These packs are excluded from loading of pack-specific string files because they consistent entirely of base game cards.
    // If you're making a pack that also consists of only base game cards, add it to this list.
    // The name and description of the pack can go in the main UIstrings.json file.
    private static final List<String> baseGamePacks = Arrays.asList(IroncladPack.class.getName(), SilentPack.class.getName(), DefectPack.class.getName(), WatcherPack.class.getName());

    public void loadPackStrings() {
        Collection<CtClass> packClasses = new AutoAdd(modID)
                .packageFilter(AbstractCardPack.class)
                .findClasses(AbstractCardPack.class)
                .stream()
                .filter(c -> !baseGamePacks.contains(c.getName()))
                .collect(Collectors.toList());
        logger.info("Found pack classes with AutoAdd: " + packClasses.size());

        for (CtClass packClass : packClasses) {
            String packName = packClass.getSimpleName().toLowerCase();
            String languageAndPack = getLangString() + "/" + packName;
            logger.info("Loading strings for pack " + packClass.getName() + "from \"resources/localization/" + languageAndPack + "\"");
            //Do not need to be checked as these always need to exist
            BaseMod.loadCustomStringsFile(CardStrings.class, modID + "Resources/localization/" + languageAndPack + "/Cardstrings.json");

            String filepath = modID + "Resources/localization/" + languageAndPack + "/";
            if (Gdx.files.internal(filepath + "Relicstrings.json").exists()) {
                BaseMod.loadCustomStringsFile(RelicStrings.class, filepath + "Relicstrings.json");
            }
            if (Gdx.files.internal(filepath + "Powerstrings.json").exists()) {
                BaseMod.loadCustomStringsFile(PowerStrings.class, filepath + "Powerstrings.json");
            }
            if (Gdx.files.internal(filepath + "UIstrings.json").exists()) {
                BaseMod.loadCustomStringsFile(UIStrings.class, filepath + "UIstrings.json");
            }
            if (Gdx.files.internal(filepath + "Stancestrings.json").exists()) {
                BaseMod.loadCustomStringsFile(StanceStrings.class, filepath + "Stancestrings.json");
            }
            if (Gdx.files.internal(filepath + "Orbstrings.json").exists()) {
                BaseMod.loadCustomStringsFile(OrbStrings.class, filepath + "Orbstrings.json");
            }
            if (Gdx.files.internal(filepath + "Potionstrings.json").exists()) {
                BaseMod.loadCustomStringsFile(PotionStrings.class, filepath + "Potionstrings.json");
            }
        }
    }

    @Override
    public void receiveEditKeywords() {
        Gson gson = new Gson();
        String json = Gdx.files.internal(modID + "Resources/localization/" + getLangString() + "/Keywordstrings.json").readString(String.valueOf(StandardCharsets.UTF_8));
        List<Keyword> keywords = new ArrayList<>(Arrays.asList(gson.fromJson(json, Keyword[].class)));

        Collection<CtClass> packClasses = new AutoAdd(modID)
                .packageFilter(AbstractCardPack.class)
                .findClasses(AbstractCardPack.class)
                .stream()
                .filter(c -> !baseGamePacks.contains(c.getName()))
                .collect(Collectors.toList());

        for (CtClass packClass : packClasses) {
            String packName = packClass.getSimpleName().toLowerCase();
            String languageAndPack = getLangString() + "/" + packName;
            logger.info("Loading keywords for pack " + packClass.getName() + "from \"resources/localization/" + languageAndPack + "\"");
            String packJson = modID + "Resources/localization/" + languageAndPack + "/Keywordstrings.json";
            FileHandle handle = Gdx.files.internal(packJson);
            if (handle.exists()) {
                packJson = handle.readString(String.valueOf(StandardCharsets.UTF_8));
                List<Keyword> packKeywords = new ArrayList<>(Arrays.asList(gson.fromJson(packJson, Keyword[].class)));
                keywords.addAll(packKeywords);
            }
        }

        for (Keyword keyword : keywords) {
            BaseMod.addKeyword(modID, keyword.PROPER_NAME, keyword.NAMES, keyword.DESCRIPTION);
        }
    }

    @Override
    public void receiveAddAudio() {
        BaseMod.addAudio(BEES_KEY, BEES_OGG);
        BaseMod.addAudio(ELEPHANT_KEY, ELEPHANT_OGG);
        BaseMod.addAudio(PEW_KEY, PEW_OGG);
        BaseMod.addAudio(EVIL_KEY, EVIL_OGG);
        BaseMod.addAudio(PANDA_KEY, PANDA_OGG);
        BaseMod.addAudio(PORCUPINE_KEY, PORCUPINE_OGG);
        BaseMod.addAudio(TRANSMUTATION_KEY, TRANSMUTATION_OGG);
        BaseMod.addAudio(WATER_IMPACT_1_KEY, WATER_IMPACT_1_OGG);
        BaseMod.addAudio(WATER_IMPACT_2_KEY, WATER_IMPACT_2_OGG);
        BaseMod.addAudio(WATER_IMPACT_3_KEY, WATER_IMPACT_3_OGG);
        BaseMod.addAudio(ICE_IMPACT_1_KEY, ICE_IMPACT_1_OGG);
        BaseMod.addAudio(ICE_IMPACT_2_KEY, ICE_IMPACT_2_OGG);
        BaseMod.addAudio(ICE_IMPACT_3_KEY, ICE_IMPACT_3_OGG);
        BaseMod.addAudio(STEAM_IMPACT_1_KEY, STEAM_IMPACT_1_OGG);
        BaseMod.addAudio(STEAM_IMPACT_2_KEY, STEAM_IMPACT_2_OGG);
        BaseMod.addAudio(STEAM_IMPACT_3_KEY, STEAM_IMPACT_3_OGG);
        BaseMod.addAudio(GUN1_KEY, GUN1_OGG);
        BaseMod.addAudio(GUN2_KEY, GUN2_OGG);
        BaseMod.addAudio(GUN3_KEY, GUN3_OGG);
        BaseMod.addAudio("UpgradesPack_ShortUpgrade","anniv5Resources/audio/UpgradesPack_ShortUpgrade.ogg");
        BaseMod.addAudio(makeID("RipPack_Rip"), makePath("audio/rippack/rip.mp3"));
        BaseMod.addAudio(makeID("RipPack_Yay"), makePath("audio/rippack/yay.ogg"));
        BaseMod.addAudio(makeID("RipPack_Ding"), makePath("audio/rippack/ding.ogg"));
        BaseMod.addAudio(makeID("RipPack_Charge"), makePath("audio/rippack/spearcharge.mp3"));
        BaseMod.addAudio(makeID("RipPack_SpearThrow"), makePath("audio/rippack/spearthrow.mp3"));
        BaseMod.addAudio(makeID("RipPack_MorningStarThrow"), makePath("audio/rippack/scythe.ogg"));
        BaseMod.addAudio(makeID("RipPack_Party"), makePath("audio/rippack/party.ogg"));
        BaseMod.addAudio(makeID("RipPack_Splash"), makePath("audio/rippack/splash.mp3"));
        BaseMod.addAudio(makeID("RipPack_Ahh"), makePath("audio/rippack/ahh.ogg"));
        BaseMod.addAudio(makeID("RipPack_Ohh"), makePath("audio/rippack/ohh.mp3"));
        BaseMod.addAudio(makeID("RipPack_Sword"), makePath("audio/rippack/sword.ogg"));
        BaseMod.addAudio(modID + "dice1",  modID + "Resources/audio/DiceRoll1.wav");
        BaseMod.addAudio(modID + "dice2",  modID + "Resources/audio/DiceRoll2.wav");
        BaseMod.addAudio(modID + "dice3",  modID + "Resources/audio/DiceRoll3.wav");
        BaseMod.addAudio(modID + "dice4",  modID + "Resources/audio/DiceRoll4.wav");
        BaseMod.addAudio(modID + "fast",  modID + "Resources/audio/rimworldpack/fast.wav");
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom room) {
        pandaList.clear();
        UltimateHomerun.HIGH_SCORE = 0;
        CLAW_SHARP_TRACKER = 0;
        combatExhausts = 0;
    }
    
	@Override
	public void receivePostExhaust(AbstractCard arg0) {
		combatExhausts++;
	}

    public static void declarePacks() {
        // We prefer to catch duplicate pack IDs here, instead of letting them break in unexpected ways downstream of this code
        packsByID = new HashMap<>();
        new AutoAdd(modID)
                .packageFilter(AbstractCardPack.class)
                .any(AbstractCardPack.class, (info, pack) -> {
                    if (packsByID.containsKey(pack.packID)) {
                        throw new RuntimeException("Duplicate pack detected with ID: " + pack.packID + ". Pack class 1: " + packsByID.get(pack.packID).getClass().getName() + ", pack class 2: " + pack.getClass().getName());
                    }
                    packsByID.put(pack.packID, pack);
                    unfilteredAllPacks.add(pack);
                    if (PackFilterMenu.getFilterConfig(pack.packID)) {
                        allPacks.add(pack);
                    }
                    packExclusivePotions.addAll(pack.getPackPotions());
                });

    }

    public static AbstractCardPack getRandomPackFromAll() {
        return allPacks.get(AbstractDungeon.cardRandomRng.random(0, allPacks.size() - 1));
    }

    public static AbstractCardPack getRandomPackFromCurrentPool() {
        return currentPoolPacks.get(AbstractDungeon.cardRandomRng.random(0, currentPoolPacks.size() - 1));
    }

    public static AbstractCard getRandomCardFromPack(AbstractCardPack pack) {
        List<AbstractCard> validCards = pack.cards
                .stream()
                .filter(c -> c.rarity == AbstractCard.CardRarity.COMMON || c.rarity == AbstractCard.CardRarity.UNCOMMON || c.rarity == AbstractCard.CardRarity.RARE)
                .filter(c -> !c.hasTag(AbstractCard.CardTags.HEALING))
                .collect(Collectors.toList());
        return validCards.get(AbstractDungeon.cardRandomRng.random(0, validCards.size() - 1)).makeCopy();
    }


    public static ArrayList<AbstractCard> getCardsFromPacks(String pack, int count) {
        ArrayList<String> quick = new ArrayList<>();
        quick.add(pack);
        return getCardsFromPacks(quick, count);
    }

    public static ArrayList<AbstractCard> getCardsFromPacks(ArrayList<String> packs, int count) {
        ArrayList<AbstractCard> cards = new ArrayList<>();
        for (String s : packs
        ) {
            AbstractCardPack p = packsByID.get(s);
            for (String s2 : p.getCards()
            ) {
                if (CardLibrary.getCard(s2).rarity == AbstractCard.CardRarity.COMMON ||
                        CardLibrary.getCard(s2).rarity == AbstractCard.CardRarity.UNCOMMON ||
                        CardLibrary.getCard(s2).rarity == AbstractCard.CardRarity.RARE) cards.add(CardLibrary.getCard(s2).makeCopy());
            }
        }

        //If count is 0 or less, return everything.
        if (count <= 0) {
            return cards;
        }

        //Otherwise make a new list with random N cards from the original list and return that
        Collections.shuffle(cards);
        ArrayList<AbstractCard> cards2 = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards2.add(cards.get(i));
        }
        return cards2;
    }

    public static ArrayList<AbstractCard> getPreviewCardsFromCurrentSet() {
        ArrayList<AbstractCard> valid = new ArrayList<>();
        for (AbstractCardPack cp : currentPoolPacks) {
            valid.add(cp.previewPackCard);
        }
        return valid;
    }


    public static ArrayList<AbstractCard> getPreviewCardsNotFromCurrentSet() {
        ArrayList<AbstractCard> valid = new ArrayList<>();
        for (AbstractCardPack cp : allPacks) {
            if (!currentPoolPacks.contains(cp)) valid.add(cp.previewPackCard);
        }
        return valid;
    }

    public static ArrayList<AbstractCardPack> getRandomPacks(boolean onlyCurrent, int count) {
        ArrayList<AbstractCardPack> allChoices = new ArrayList<>();
        ArrayList<AbstractCardPack> valid = new ArrayList<>();

        if (onlyCurrent) {
            allChoices.addAll(SpireAnniversary5Mod.currentPoolPacks);
        } else {
            allChoices.addAll(SpireAnniversary5Mod.allPacks);
        }

        for (int i = 0; i < count; i++) {
            AbstractCardPack p = allChoices.get(AbstractDungeon.cardRandomRng.random(0, allChoices.size() - 1));
            valid.add(p);
            allChoices.remove(p);
        }
        return valid;
    }

    public static void startOfGamePackSetup() {
        currentPoolPacks.clear();
        selectedCards = false;

        ArrayList<String> packSetup = new ArrayList<>();

        if (MainMenuUIPatch.customDraft) {
            packSetup.addAll(MainMenuUIPatch.packSetups);
        } else {
            packSetup.add(CoreSetPack.ID);
            packSetup.add(RANDOM);
            packSetup.add(RANDOM);
            packSetup.add(RANDOM);
            packSetup.add(RANDOM);
            packSetup.add(CHOICE);
            packSetup.add(CHOICE);
        }

        int randomsToSetup = 0;
        int choicesToSetup = 0;

        for (String setupType : packSetup) {
            logger.info("Setting up Pack type " + setupType + ".");

            switch (setupType) {
                case RANDOM:
                    randomsToSetup++;
                    break;
                case CHOICE:
                    choicesToSetup++;
                    break;
                default:
                    for (AbstractCardPack pack : unfilteredAllPacks) {
                        if (pack.packID.equals(setupType)) {
                            logger.info("Found pack matching name " + pack.name);
                            currentPoolPacks.add(pack);
                        }
                    }
            }
        }

        BaseMod.openCustomScreen(PackSetupScreen.Enum.PACK_SETUP_SCREEN, randomsToSetup, choicesToSetup);
    }

    private static void startOfGamePackReveals() {
        logger.info("Total packs: " + currentPoolPacks.toString());
        CardGroup packDisplays = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);

        if (currentPoolPacks.size() != PACKS_PER_RUN) {
            logger.error( MessageFormat.format("Less packs in pool than expected: {0}/{1}", currentPoolPacks.size(), PACKS_PER_RUN));
        }

        for (AbstractCardPack pack : currentPoolPacks) {
            packDisplays.addToTop(pack.previewPackCard);
        }

        AbstractDungeon.gridSelectScreen.open(packDisplays, 0, true, CardCrawlGame.languagePack.getUIString(makeID("AtGameStart")).TEXT[0]);
        //Calling this to fill the card pool after the currentPoolPacks are filled
        selectedCards = true;
        CardCrawlGame.dungeon.initializeCardPools();
    }

    @Override
    public void receivePostUpdate() {
        if (!openedStarterScreen) {
            if (CardCrawlGame.isInARun() && doPackSetup && !AbstractDungeon.isScreenUp) {
                logger.info("Starting Packmaster setup.");
                startOfGamePackSetup();
                openedStarterScreen = true;
            }
        }
    }

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {
        DeepDreamPatch.wakeUp();
        ImproveEffect._clean();
        DynamicDynamicVariableManager.clearVariables();
        combatExhausts=0;
    }

    @Override
    public void receivePostPowerApplySubscriber(AbstractPower power, AbstractCreature target, AbstractCreature source) {
        if (power.type == AbstractPower.PowerType.DEBUFF && source == AbstractDungeon.player && target != AbstractDungeon.player) {
            // Biting Cold Pack
            // Growing Affliction (Return to hand)
            for (AbstractCard c : AbstractDungeon.player.discardPile.group)
                if (c.cardID.equals(GrowingAffliction.ID))
                    AbstractDungeon.actionManager.addToBottom(new DiscardToHandAction(c));

            // Glaciate (Gain Vigor)
            if (power.ID.equals(FrostbitePower.POWER_ID) && source.hasPower(GlaciatePower.POWER_ID)) {
                AbstractPower glaciate = source.getPower(GlaciatePower.POWER_ID);

                atb(new AbstractGameAction() {
                    @Override
                    public void update() {
                        glaciate.flash();
                        if (Settings.FAST_MODE)
                            addToBot(new WaitAction(0.1F));
                        else
                            addToBot(new WaitAction(0.2F));
                        applyToSelf(new VigorPower(AbstractDungeon.player, glaciate.amount));
                        this.isDone = true;
                    }
                });
            }

            //Ring of Pain pack
            if (!target.hasPower(ArtifactPower.POWER_ID)) {
                atb(new AbstractGameAction() {
                    @Override
                    public void update() {
                        for (AbstractCard card : adp().hand.group) {
                            if (card instanceof Slime) {
                                ((Slime) card).triggerOnDebuff();
                            }
                        }
                        this.isDone = true;
                    }
                });
            }
        }
    }

    @Override
    public ArrayList<String> onSave() {
        ArrayList<String> packIDs = new ArrayList<>();
        for (AbstractCardPack pack : currentPoolPacks) {
            packIDs.add(pack.packID);
        }
        return packIDs;
    }

    @Override
    public void onLoad(ArrayList<String> strings) {
        currentPoolPacks.clear();
        if (strings != null) {
            for (String s : strings) {
                currentPoolPacks.add(packsByID.get(s));
            }
        }
    }

    @Override
    public void receiveStartGame() {
        BaseMod.removeTopPanelItem(currentRunCardsTopPanelItem);
        if (AbstractDungeon.player.chosenClass == ThePackmaster.Enums.THE_PACKMASTER) {
            BaseMod.addTopPanelItem(currentRunCardsTopPanelItem);
        }
    }

    public static class Enums {
        @SpireEnum
        public static AbstractGameAction.AttackEffect EVIL;
    }

}
