package thePackmaster.cards;

import basemod.abstracts.CustomCard;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;
import thePackmaster.ThePackmaster;
import thePackmaster.packs.AbstractCardPack;
import thePackmaster.util.CardArtRoller;
import thePackmaster.util.Wiz;

import static thePackmaster.SpireAnniversary5Mod.makeImagePath;
import static thePackmaster.SpireAnniversary5Mod.modID;
import static thePackmaster.util.Wiz.atb;
import static thePackmaster.util.Wiz.att;

public abstract class AbstractPackmasterCard extends CustomCard {

    protected final CardStrings cardStrings;

    public int secondMagic;
    public int baseSecondMagic;
    public boolean upgradedSecondMagic;
    public boolean isSecondMagicModified;

    public int secondDamage;
    public int baseSecondDamage;
    public boolean upgradedSecondDamage;
    public boolean isSecondDamageModified;

    private boolean needsArtRefresh = false;
    private String bottomText;

    public boolean isUnnate = false;

    public static Color packNameDisplayColor = Settings.CREAM_COLOR.cpy();

    public AbstractPackmasterCard(final String cardID, final int cost, final CardType type, final CardRarity rarity, final CardTarget target) {
        this(cardID, cost, type, rarity, target, ThePackmaster.Enums.PACKMASTER_RAINBOW);
    }

    public AbstractPackmasterCard(final String cardID, final int cost, final CardType type, final CardRarity rarity, final CardTarget target, final CardColor color) {
        this(cardID, cost, type, rarity, target, color, getCardTextureString(cardID.replace(modID + ":", ""), type));
    }

    public AbstractPackmasterCard(final String cardID, final int cost, final CardType type, final CardRarity rarity, final CardTarget target, final CardColor color, final String textureString) {
        super(cardID, "", textureString, cost, "", type, color, rarity, target);
        cardStrings = CardCrawlGame.languagePack.getCardStrings(this.cardID);
        rawDescription = cardStrings.DESCRIPTION;
        name = originalName = cardStrings.NAME;
        initializeTitle();
        initializeDescription();

        if (textureImg.contains("ui/missing.png")) {
            if (CardLibrary.getAllCards() != null && !CardLibrary.getAllCards().isEmpty()) {
                CardArtRoller.computeCard(this);
            } else
                needsArtRefresh = true;
        }

        setBackgroundTexture(
                "anniv5Resources/images/512/coreset/" + type.name().toLowerCase() + ".png",
                "anniv5Resources/images/1024/coreset/" + type.name().toLowerCase() + ".png"
        );
    }

    @Override
    protected Texture getPortraitImage() {
        if (textureImg.contains("ui/missing.png")) {
            return CardArtRoller.getPortraitTexture(this);
        } else {
            return super.getPortraitImage();
        }
    }

    public static String getCardTextureString(final String cardName, final AbstractCard.CardType cardType) {
        String textureString = makeImagePath("cards/" + cardName + ".png");

        FileHandle h = Gdx.files.internal(textureString);
        if (!h.exists()) {
            textureString = makeImagePath("ui/missing.png");
        }
        return textureString;
    }

    @Override
    public void applyPowers() {
        if (baseSecondDamage > -1) {
            secondDamage = baseSecondDamage;

            int tmp = baseDamage;
            baseDamage = baseSecondDamage;

            super.applyPowers();

            secondDamage = damage;
            baseDamage = tmp;

            super.applyPowers();

            isSecondDamageModified = (secondDamage != baseSecondDamage);
        } else super.applyPowers();
    }

    @Override
    public void calculateCardDamage(AbstractMonster mo) {
        if (baseSecondDamage > -1) {
            secondDamage = baseSecondDamage;

            int tmp = baseDamage;
            baseDamage = baseSecondDamage;

            super.calculateCardDamage(mo);

            secondDamage = damage;
            baseDamage = tmp;

            super.calculateCardDamage(mo);

            isSecondDamageModified = (secondDamage != baseSecondDamage);
        } else super.calculateCardDamage(mo);
    }

    public void resetAttributes() {
        super.resetAttributes();
        secondMagic = baseSecondMagic;
        isSecondMagicModified = false;
        secondDamage = baseSecondDamage;
        isSecondDamageModified = false;
    }

    public void displayUpgrades() {
        super.displayUpgrades();
        if (upgradedSecondMagic) {
            secondMagic = baseSecondMagic;
            isSecondMagicModified = true;
        }
        if (upgradedSecondDamage) {
            secondDamage = baseSecondDamage;
            isSecondDamageModified = true;
        }
    }

    protected void upgradeSecondMagic(int amount) {
        baseSecondMagic += amount;
        secondMagic = baseSecondMagic;
        upgradedSecondMagic = true;
    }

    protected void upgradeSecondDamage(int amount) {
        baseSecondDamage += amount;
        secondDamage = baseSecondDamage;
        upgradedSecondDamage = true;
    }

    protected void uDesc() {
        rawDescription = cardStrings.UPGRADE_DESCRIPTION;
        initializeDescription();
    }

    public void downgrade() {
        if (upgraded) {
            upgraded = false;
            name = cardStrings.NAME;
            rawDescription = cardStrings.DESCRIPTION;
            initializeTitle();
            initializeDescription();
        }
    }

    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            upp();
            if (cardStrings.UPGRADE_DESCRIPTION != null) {
                uDesc();
            }
        }
    }

    public abstract void upp();

    public void update() {
        super.update();
        if (needsArtRefresh) {
            CardArtRoller.computeCard(this);
        }
    }

    // These shortcuts are specifically for cards. All other shortcuts that aren't specifically for cards can go in Wiz.
    protected void dmg(AbstractMonster m, AbstractGameAction.AttackEffect fx) {
        atb(new DamageAction(m, new DamageInfo(AbstractDungeon.player, damage, damageTypeForTurn), fx));
    }

    protected void dmgTop(AbstractMonster m, AbstractGameAction.AttackEffect fx) {
        att(new DamageAction(m, new DamageInfo(AbstractDungeon.player, damage, damageTypeForTurn), fx));
    }

    protected void allDmg(AbstractGameAction.AttackEffect fx) {
        atb(new DamageAllEnemiesAction(AbstractDungeon.player, multiDamage, damageTypeForTurn, fx));
    }

    protected void allDmgTop(AbstractGameAction.AttackEffect fx) {
        att(new DamageAllEnemiesAction(AbstractDungeon.player, multiDamage, damageTypeForTurn, fx));
    }

    protected void altDmg(AbstractMonster m, AbstractGameAction.AttackEffect fx) {
        atb(new DamageAction(m, new DamageInfo(AbstractDungeon.player, secondDamage, damageTypeForTurn), fx));
    }

    protected void blck() {
        atb(new GainBlockAction(AbstractDungeon.player, AbstractDungeon.player, block));
    }

    public String cardArtCopy() {
        return null;
    }

    public CardArtRoller.ReskinInfo reskinInfo(String ID) {
        return null;
    }

    protected void upMagic(int x) {
        upgradeMagicNumber(x);
    }

    protected void upSecondMagic(int x) {
        upgradeSecondMagic(x);
    }

    protected void upSecondDamage(int x) {
        upgradeSecondDamage(x);
    }

    public AbstractCardPack getParent() {
        return Wiz.getPackByCard(this);
    }

    public String getParentName() {
        AbstractCardPack p = getParent();
        if(p != null)
            return p.name;
        return "No Parent Pack!";
    }

    public void renderBorderText(SpriteBatch sb, boolean renderBottom) {
        String text = renderBottom? getBottomText() : getTopText();
        if (text != null) {
            float xPos, yPos, offsetY;
            BitmapFont font;
            if (this.isFlipped || this.isLocked || this.transparency <= 0.0F)
                return;
            font = FontHelper.cardTitleFont;
            xPos = this.current_x;
            yPos = this.current_y;
            float yOffsetBase = 400;
            if(renderBottom) {
                yOffsetBase *= -1;
                yOffsetBase += 15f;
            }
            offsetY = yOffsetBase * Settings.scale * this.drawScale / 2.0F;
            BitmapFont.BitmapFontData fontData = font.getData();
            float originalScale = fontData.scaleX;
            float scaleMulti = 0.8F;
            int length = text.length();
            if (length > 20) {
                scaleMulti -= 0.02F * (length - 20);
                if (scaleMulti < 0.5F)
                    scaleMulti = 0.5F;
            }
            fontData.setScale(scaleMulti * (this.drawScale * 0.85f));
            Color color = renderBottom? getBottomTextColor().cpy() : getTopTextColor().cpy();
            color.a = this.transparency;
            FontHelper.renderRotatedText(sb, font, text, xPos, yPos, 0.0F, offsetY, this.angle, true, color);
            fontData.setScale(originalScale);
        }
    }

    public String getBottomText() {return null;}

    public String getTopText() {
        AbstractCardPack parent = getParent();
        if(parent != null) {
            return parent.name;
        }

        return null;
    }

    public Color getTopTextColor() {
        return packNameDisplayColor;
    }

    public Color getBottomTextColor() {
        return Settings.CREAM_COLOR;
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
        renderBorderText(sb, false);
        renderBorderText(sb, true);
    }

    @Override
    public void renderInLibrary(SpriteBatch sb) {
        super.renderInLibrary(sb);
        if (!(SingleCardViewPopup.isViewingUpgrade && this.isSeen && !this.isLocked)) {
            renderBorderText(sb, false);
            renderBorderText(sb, true);
        }
    }
}
