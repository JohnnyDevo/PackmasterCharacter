package thePackmaster.packs;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.FocusPotion;
import com.megacrit.cardcrawl.potions.PotionOfCapacity;
import thePackmaster.SpireAnniversary5Mod;
import thePackmaster.cards.summonspack.*;

import java.util.ArrayList;

public class SummonsPack extends AbstractCardPack {
    public static final String ID = SpireAnniversary5Mod.makeID(SummonsPack.class.getSimpleName());
    private static final UIStrings UI_STRINGS = CardCrawlGame.languagePack.getUIString(ID);
    public static final String NAME = UI_STRINGS.TEXT[0];
    public static final String DESC = UI_STRINGS.TEXT[1];
    public static final String AUTHOR = UI_STRINGS.TEXT[2];

    public SummonsPack() {
        super(ID, NAME, DESC, AUTHOR);
    }

    @Override
    public ArrayList<String> getCards() {
        ArrayList<String> cards = new ArrayList<>();
        cards.add(EvilSpirit.ID);
        cards.add(SummonWolf.ID);
        cards.add(SummonPorcupine.ID);
        cards.add(SummonElephant.ID);
        cards.add(FireSpirit.ID);
        cards.add(RainbowLouse.ID);
        cards.add(SummonBees.ID);
        cards.add(Ambush.ID);
        cards.add(SummonPandas.ID);
        cards.add(Control.ID);
        cards.add(Quill.ID);
        return cards;
    }

    @Override
    public ArrayList<String> getPackPotions() {
        ArrayList<String> list = new ArrayList<>();
        list.add(FocusPotion.POTION_ID);
        list.add(PotionOfCapacity.POTION_ID);
        return list;
    }
}
