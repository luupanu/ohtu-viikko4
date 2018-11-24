package ohtu;

import ohtu.verkkokauppa.Kauppa;
import ohtu.verkkokauppa.Pankki;
import ohtu.verkkokauppa.Tuote;
import ohtu.verkkokauppa.Varasto;
import ohtu.verkkokauppa.Viitegeneraattori;

import org.junit.*;
import static org.mockito.Mockito.*;

public class KauppaTest {

  private Pankki pankki;
  private Viitegeneraattori viite;
  private Varasto varasto;
  private Kauppa kauppa;

  private final static int VIITENUMERO = 42;
  private final static String ASIAKAS = "pekka";
  private final static String TILINUMERO = "12345";

  @Before
  public void setUp() {
    pankki = mock(Pankki.class);
    viite = mock(Viitegeneraattori.class);
    varasto = mock(Varasto.class);
    kauppa = new Kauppa(varasto, pankki, viite);

    lisaaVarastoon();
    when(viite.uusi()).thenReturn(VIITENUMERO);
  }

  @Test
  public void aloitaAsiointiNollaaEdellisenOstoksenTiedot() {
    kauppa.aloitaAsiointi();
    kauppa.lisaaKoriin(1);
    kauppa.lisaaKoriin(2);
    kauppa.lisaaKoriin(2);

    kauppa.aloitaAsiointi();
    kauppa.lisaaKoriin(2);
    kauppa.tilimaksu(ASIAKAS, TILINUMERO);

    verify(pankki).tilisiirto(
      eq(ASIAKAS),
      eq(VIITENUMERO),
      eq(TILINUMERO),
      anyString(),
      eq(8)
    );
  }

  @Test
  public void kauppaPyytaaUudenViitenumeronJokaMaksutapahtumalle() {
    kauppa.aloitaAsiointi();
    kauppa.lisaaKoriin(1);
    kauppa.tilimaksu(ASIAKAS, TILINUMERO);

    kauppa.aloitaAsiointi();
    kauppa.lisaaKoriin(2);
    kauppa.tilimaksu(ASIAKAS, TILINUMERO);

    verify(viite, times(2)).uusi();
  }

  @Test
  public void tuotteenPoistaminenOstoskoristaOnnistuu() {
    kauppa.aloitaAsiointi();
    kauppa.lisaaKoriin(1);
    kauppa.lisaaKoriin(2);
    kauppa.lisaaKoriin(2);
    kauppa.poistaKorista(2);
    kauppa.tilimaksu(ASIAKAS, TILINUMERO);

    verify(pankki).tilisiirto(
      eq(ASIAKAS),
      eq(VIITENUMERO),
      eq(TILINUMERO),
      anyString(),
      eq(13)
    );
  }

  @Test
  public void yhdenTuotteenOstamisenJalkeenPankinMetodiaTilisiirtoKutsutaanOikein() {
    kauppa.aloitaAsiointi();
    kauppa.lisaaKoriin(1);
    kauppa.tilimaksu(ASIAKAS, TILINUMERO);

    verify(pankki).tilisiirto(
      eq(ASIAKAS),
      eq(VIITENUMERO),
      eq(TILINUMERO),
      anyString(),
      eq(5)
    );
  }

  @Test
  public void kahdenEriTuotteenOstamisenJalkeenPankinMetodiaTilisiirtoKutsutaanOikein() {
    kauppa.aloitaAsiointi();
    kauppa.lisaaKoriin(1);
    kauppa.lisaaKoriin(2);
    kauppa.tilimaksu(ASIAKAS, TILINUMERO);

    verify(pankki).tilisiirto(
      eq(ASIAKAS),
      eq(VIITENUMERO),
      eq(TILINUMERO),
      anyString(),
      eq(13)
    );
  }

  @Test
  public void kahdenSamanTuotteenOstamisenJalkeenPankinMetodiaTilisiirtoKutsutaanOikein() {
    kauppa.aloitaAsiointi();
    kauppa.lisaaKoriin(1);
    kauppa.lisaaKoriin(1);
    kauppa.tilimaksu(ASIAKAS, TILINUMERO);

    verify(pankki).tilisiirto(
      eq(ASIAKAS),
      eq(VIITENUMERO),
      eq(TILINUMERO),
      anyString(),
      eq(10)
    );
  }

  @Test
  public void kahdenTuotteenJostaToinenOnLoppuOstamisenJalkeenPankinMetodiaTilisiirtoKutsutaanOikein() {
    kauppa.aloitaAsiointi();
    kauppa.lisaaKoriin(2);
    kauppa.lisaaKoriin(3);
    kauppa.tilimaksu(ASIAKAS, TILINUMERO);

    verify(pankki).tilisiirto(
      eq(ASIAKAS),
      eq(VIITENUMERO),
      eq(TILINUMERO),
      anyString(),
      eq(8)
    );
  }

  private void lisaaVarastoon() {
    // maitoa 10 kpl, hinta 5
    when(varasto.saldo(1)).thenReturn(10);
    when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
    // olutta 2 kpl, hinta 8
    when(varasto.saldo(2)).thenReturn(2);
    when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "olut", 8));
    // piimää 0 kpl, hinta 13
    when(varasto.saldo(3)).thenReturn(0);
    when(varasto.haeTuote(3)).thenReturn(new Tuote(3, "piimä", 13));
  }
}