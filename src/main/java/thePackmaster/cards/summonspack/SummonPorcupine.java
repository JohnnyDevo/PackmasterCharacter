package thePackmaster.cards.summonspack;

import com.evacipated.cardcrawl.mod.stslib.patches.FlavorText;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import thePackmaster.cards.AbstractPackmasterCard;
import thePackmaster.orbs.summonspack.Porcupine;
import thePackmaster.powers.summonspack.TreeBlockadePower;
import thePackmaster.util.Wiz;

import static thePackmaster.SpireAnniversary5Mod.makeID;
import static thePackmaster.cards.summonspack.FlavorConstants.FLAVOR_BOX_COLOR;
import static thePackmaster.cards.summonspack.FlavorConstants.FLAVOR_TEXT_COLOR;
import static thePackmaster.util.Wiz.adp;
import static thePackmaster.util.Wiz.atb;

public class SummonPorcupine extends AbstractPackmasterCard {
    public final static String ID = makeID(SummonPorcupine.class.getSimpleName());
    private static final int COST = 1;
    private static final CardType TYPE = CardType.SKILL;
    private static final CardRarity RARITY = CardRarity.COMMON;
    private static final CardTarget TARGET = CardTarget.SELF;

    private static final int MAGIC = 1;
    private static final int UPGRADE_MAGIC = 1;

    public SummonPorcupine() {
        super(ID, COST, TYPE, RARITY, TARGET);
        baseMagicNumber = magicNumber = MAGIC;
        FlavorText.AbstractCardFlavorFields.boxColor.set(this, FLAVOR_BOX_COLOR);
        FlavorText.AbstractCardFlavorFields.textColor.set(this, FLAVOR_TEXT_COLOR);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        atb(new ChannelAction(new Porcupine()));
        Wiz.makeInHand(new Quill(), magicNumber);
    }

    @Override
    public void upp() {
        upMagic(UPGRADE_MAGIC);
    }
}
