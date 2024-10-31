package steps;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServerHasNotBeenStartedLocallyException;
import io.cucumber.java.*;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import static java.time.Duration.ofMillis;
import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Log4j2
public class Hooks {
	/**
	 * Declaración de variables estáticas y constantes.
	 */
	protected static AndroidDriver driver;
	protected static AppiumDriverLocalService appiumServer;
	protected static WebDriverWait wait;
	protected static Actions action;
	protected final static PointerInput FINGER = new PointerInput(TOUCH, "FINGER");
	public static String aux = null;
	public static String auxTwo = null;
	public static int auxint = 0;
	public static int auxTwoint = 0;
	private static final String PLATFORM_NAME = "Android";
	private static final String APP_PACKAGE = "com.example.poke_app";
	private static final String APP_ACTIVITY = ".MainActivity";
	private static final String APP_PATH = "src/test/resources/app/app-release.apk";
	private static final String APPIUM_SERVER_URL = "http://127.0.0.1:4723/";

	/**
	 * Inicializa el servidor de Appium.
	 */
	@BeforeAll
	public static void startAppiumServer() {
		try {
			log.info("Iniciando el servidor de Appium");
			appiumServer = AppiumDriverLocalService.buildDefaultService();
			appiumServer.start();
			appiumServer.clearOutPutStreams();
			addEmergencyShutdown();
		} catch (AppiumServerHasNotBeenStartedLocallyException e) {
			throw new RuntimeException("Error al iniciar el servidor de Appium", e);
		}
	}

	/**
	 * Configura el entorno antes de ejecutar las pruebas.
	 * Crea las capacidades, inicializa el driver de Appium y registra un mensaje informativo.
	 */
	@Before
	public void setUp() {
		UiAutomator2Options options = createCapabilities(true);
		initializeDriver(options);
		log.info("Iniciando la aplicación");
	}

	/**
	 * Crea las capacidades de la aplicación según la configuración especificada.
	 * @param autoGrantPermissions Indica si se deben conceder permisos automáticamente.
	 * @return Las capacidades configuradas.
	 */
	private UiAutomator2Options createCapabilities(boolean autoGrantPermissions) {
		UiAutomator2Options options = new UiAutomator2Options();
		options.setPlatformName(PLATFORM_NAME);
		options.setAutomationName(AutomationName.ANDROID_UIAUTOMATOR2);
		options.setAppPackage(APP_PACKAGE);
		options.setAppActivity(APP_ACTIVITY);
		options.setNoReset(false);
		options.setApp(String.valueOf(Paths.get(APP_PATH).toAbsolutePath()));
		options.setAutoGrantPermissions(autoGrantPermissions);

		return options;
	}

	/**
	 * Reinicia la aplicación con capacidades modificadas.
	 * Crea nuevas capacidades, cierra la aplicación actual, inicializa el driver de Appium con las nuevas capacidades
	 * y registra un mensaje informativo.
	 */
	public void restartAppWithModifiedCapabilities() {
		UiAutomator2Options options = createCapabilities(false);
		closeApp();
		initializeDriver(options);
		log.info("Reiniciando la aplicación con capacidades modificadas");
	}

	/**
	 * Inicializa el servidor y driver de Appium con las capacidades proporcionadas.
	 * @param options Las capacidades de la aplicación.
	 */
	private void initializeDriver(UiAutomator2Options options) {
		try {
			log.info("Iniciando el driver de Appium");
			URL androidDriver = new URL(APPIUM_SERVER_URL);
			driver = new AndroidDriver(androidDriver, options);
			action = new Actions(driver);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Error al iniciar el driver", e);
		}
	}

	/**
	 * Después de las pruebas, realiza la limpieza y captura de pantalla en caso de fallo.
	 * @param scenario El escenario de prueba.
	 */
	@After
	public void tearDown(Scenario scenario) {
		if (scenario.isFailed()) {
			captureScreenshotAndAttachToReport(scenario);
		}
		closeApp();
	}

	/**
	 * Detiene el servidor de Appium al finalizar las pruebas.
	 */
	@AfterAll
	public static void stopAppiumServer() {
		appiumServer.stop();
	}

	/**
	 * Agrega un cierre de emergencia para detener el servidor de Appium al finalizar las pruebas.
	 */
	private static void addEmergencyShutdown() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (appiumServer != null && appiumServer.isRunning()) {
				log.info("Deteniendo de emergencia el servidor de Appium");
				appiumServer.stop();
			}
		}));
	}

	/**
	 * Captura de pantalla en caso de fallo y adjunta al informe.
	 * @param scenario El escenario de prueba.
	 */
	private void captureScreenshotAndAttachToReport(Scenario scenario) {
		try {
			byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
			scenario.attach(screenshot, "image/png", "El escenario falló, consulte la imagen adjunta.");
		} catch (Exception e) {
			log.error("Error al capturar la pantalla: {}", e.getMessage());
		}
	}

	/**
	 * Cierra la aplicación al finalizar las pruebas.
	 */
	public static void closeApp() {
		if (driver != null) {
			try {
				driver.quit();
				driver = null;
			} catch (WebDriverException e) {
				throw new RuntimeException("Error al cerrar la aplicación: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Encuentra un elemento en la página por su locator utilizando XPath.
	 * @param locator El locator utilizado para encontrar el elemento.
	 * @return El elemento encontrado.
	 * @throws NoSuchElementException Si el elemento no se encuentra o no es visible.
	 */
	private WebElement find(String locator) {
		try {
			wait = new WebDriverWait(driver, Duration.ofSeconds(15));
			return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
		} catch (TimeoutException e) {
			throw new NoSuchElementException("Elemento no encontrado o no visible: " + locator);
		}
	}

	/**
	 * Encuentra varios elementos en la página por su locator utilizando XPath.
	 * @param locator El locator utilizado para encontrar los elementos.
	 * @return Una lista de elementos encontrados.
	 * @throws NoSuchElementException Si los elementos no se encuentran o no son visibles.
	 */
	private List<WebElement> finds(String locator) {
		try {
			wait = new WebDriverWait(driver, Duration.ofSeconds(15));
			return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(locator)));
		} catch (TimeoutException e) {
			throw new NoSuchElementException("Elementos no encontrados o no visibles: " + locator);
		}
	}

	/**
	 * Realiza clic en un elemento identificado por su locator.
	 * @param locator El locator del elemento en el que se realizará clic.
	 */
	public void clickElement(String locator) {
		try {
			find(locator).click();
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException("Elemento no encontrado: " + locator);
		} catch (ElementNotInteractableException e) {
			throw new ElementNotInteractableException("El elemento no es interactuable: " + locator);
		} catch (StaleElementReferenceException e) {
			throw new StaleElementReferenceException("El elemento se volvió obsoleto: " + locator);
		}
	}

	/**
	 * Realiza clic en un elemento utilizando acciones (por ejemplo, moverse antes de hacer clic).
	 * @param element El locator del elemento en el que se realizará clic.
	 */
	public void clickElementActions(String element) {
		try {
			WebElement targetElement = find(element);
			action.moveToElement(targetElement).click().perform();
		} catch (NoSuchElementException e) {
			System.err.println("Excepción: El elemento no fue encontrado. Detalles: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Excepción no esperada. Detalles: " + e.getMessage());
		}
	}

	/**
	 * Escribe texto en un elemento identificado por su locator.
	 * @param locator      El locator del elemento en el que se escribirá el texto.
	 * @param textToWrite  El texto que se escribirá en el elemento.
	 * @throws NoSuchElementException          Si el elemento no se encuentra.
	 * @throws ElementNotInteractableException Si el elemento no es interactuable.
	 */
	public void write(String locator, String textToWrite) {
		try {
			find(locator).clear();
			find(locator).sendKeys(textToWrite);
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException("Elemento no encontrado: " + locator);
		} catch (ElementNotInteractableException e) {
			throw new ElementNotInteractableException("El elemento no es interactuable: " + locator);
		}
	}
	/**
	 * Escribe texto en un elemento identificado por su locator.
	 * Sin eliminar el texto que precede
	 * @param locator      El locator del elemento en el que se escribirá el texto.
	 * @param textToWrite  El texto que se escribirá en el elemento.
	 * @throws NoSuchElementException          Si el elemento no se encuentra.
	 * @throws ElementNotInteractableException Si el elemento no es interactuable.
	 */
	public void writeOver(String locator, String textToWrite) {
		try {
			find(locator).sendKeys(textToWrite);
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException("Elemento no encontrado: " + locator);
		} catch (ElementNotInteractableException e) {
			throw new ElementNotInteractableException("El elemento no es interactuable: " + locator);
		}
	}

	/**
	 * Escribe en el teclado sin especificar un locator específico.
	 * @param locator El texto que se escribirá en el teclado.
	 * @throws RuntimeException Si no se puede escribir en el teclado debido a que el elemento no es interactuable
	 *                        		o si ocurre un error inesperado al escribir en el teclado.
	 */
	public void writeKeyboard(String locator) {
		try {
			action.sendKeys(locator).perform();
		} catch (ElementNotInteractableException e) {
			throw new RuntimeException("No se pudo escribir en el teclado. Elemento no interactuable.", e);
		} catch (Exception e) {
			throw new RuntimeException("Ocurrió un error al escribir en el teclado.", e);
		}
	}

	/**
	 * Oculta el teclado en dispositivos móviles.
	 * @throws RuntimeException Si ocurre un error al intentar ocultar el teclado.
	 */
	public void hideKeyboard() {
		try {
			driver.hideKeyboard();
		} catch (Exception e) {
			throw new RuntimeException("Error al ocultar el teclado: " + e.getMessage(), e);
		}
	}

	/**
	 * Carga un archivo en el navegador móvil desde una ubicación local a una ubicación remota.
	 * @param localFileName Nombre del archivo en la ubicación local.
	 * @param remotePath    Ruta remota donde se debe almacenar el archivo en el navegador móvil.
	 * @throws RuntimeException Si ocurre un error durante la carga del archivo.
	 */
	public void loadFile(String localFileName, String remotePath) {
		try {
			File localFile = new File(localFileName);
			driver.pushFile(remotePath, localFile);
		} catch (IOException e) {
            log.error("Error al cargar el archivo: {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Simula un toque en una ubicación específica en la pantalla táctil del dispositivo móvil.
	 * @param xPercent Coordenada X en porcentaje de la anchura de la pantalla donde se realizará el toque.
	 * @param yPercent Coordenada Y en porcentaje de la altura de la pantalla donde se realizará el toque.
	 */
	public void touch(double xPercent, double yPercent) {
		try {
			Dimension size = driver.manage().window().getSize();
			int x = (int) (size.width * xPercent);
			int y = (int) (size.height * yPercent);

			PointerInput finger = new PointerInput(TOUCH, "FINGER");
			Sequence tap = new Sequence(finger, 1);
			tap.addAction(finger.createPointerMove(ofMillis(0), viewport(), x, y));
			tap.addAction(finger.createPointerDown(LEFT.asArg()));
			tap.addAction(new Pause(FINGER, ofMillis(0)));
			tap.addAction(finger.createPointerUp(LEFT.asArg()));

			driver.perform(List.of(tap));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Realiza un desplazamiento en la pantalla táctil desde un punto inicial hasta un punto final.
	 * @param startXPerc Porcentaje de la anchura de la pantalla donde inicia el desplazamiento.
	 * @param startYPerc Porcentaje de la altura de la pantalla donde inicia el desplazamiento.
	 * @param endXPerc   Porcentaje de la anchura de la pantalla donde finaliza el desplazamiento.
	 * @param endYPerc   Porcentaje de la altura de la pantalla donde finaliza el desplazamiento.
	 */
	public void swipe(double startXPerc, double startYPerc, double endXPerc, double endYPerc) {
		try {
			Dimension size = driver.manage().window().getSize();
			int startX = (int) (size.width * startXPerc);
			int startY = (int) (size.height * startYPerc);
			int endX = (int) (size.width * endXPerc);
			int endY = (int) (size.height * endYPerc);

			PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "FINGER");
			Sequence swipe = new Sequence(finger, 0);
			swipe.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), startX, startY));
			swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
			swipe.addAction(new Pause(finger, Duration.ofMillis(400)));
			swipe.addAction(finger.createPointerMove(Duration.ofMillis(400), PointerInput.Origin.viewport(), endX, endY));
			swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

			driver.perform(List.of(swipe));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Realiza un desplazamiento en la pantalla táctil desde un punto inicial hasta un punto final.
	 * @param startXPerc Porcentaje de la anchura de la pantalla donde inicia el desplazamiento.
	 * @param startYPerc Porcentaje de la altura de la pantalla donde inicia el desplazamiento.
	 * @param endXPerc   Porcentaje de la anchura de la pantalla donde finaliza el desplazamiento.
	 * @param endYPerc   Porcentaje de la altura de la pantalla donde finaliza el desplazamiento.
	 * @param time 		 Tiempo que se tarda en hacer el swipe en milisegundos.
	 */
	public void swipe(double startXPerc, double startYPerc, double endXPerc, double endYPerc, int time) {
		try {
			Dimension size = driver.manage().window().getSize();
			int startX = (int) (size.width * startXPerc);
			int startY = (int) (size.height * startYPerc);
			int endX = (int) (size.width * endXPerc);
			int endY = (int) (size.height * endYPerc);

			PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "FINGER");
			Sequence swipe = new Sequence(finger, 0);
			swipe.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), startX, startY));
			swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
			swipe.addAction(new Pause(finger, Duration.ofMillis(time)));
			swipe.addAction(finger.createPointerMove(Duration.ofMillis(time), PointerInput.Origin.viewport(), endX, endY));
			swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

			driver.perform(List.of(swipe));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Realiza un toque y mantiene presionado en una ubicación específica en la pantalla.
	 * @param xPercent Porcentaje de la anchura de la pantalla donde se realizará el toque.
	 * @param yPercent Porcentaje de la altura de la pantalla donde se realizará el toque.
	 */
	public void touchAndHold(double xPercent, double yPercent) {
		try {
			PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "FINGER");
			Sequence tap = new Sequence(finger, 1);

			Dimension size = driver.manage().window().getSize();
			int x = (int) (size.width * xPercent);
			int y = (int) (size.height * yPercent);

			tap.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), x, y));
			tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
			tap.addAction(new Pause(finger, Duration.ofMillis(2000)));
			tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

			driver.perform(List.of(tap));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Realiza un toque y mantiene presionado en un elemento específico.
	 * @param locator El locator del elemento en el que se realizará el toque y mantendrá presionado.
	 */
	public void touchAndHolElement(String locator) {
		try {
			Point location = find(locator).getLocation();

			int x = location.getX() + 50;
			int y = location.getY() + 50;

			PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "FINGER");
			Sequence tap = new Sequence(finger, 1);

			tap.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), x, y));
			tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
			tap.addAction(new Pause(finger, Duration.ofMillis(1000)));
			tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

			driver.perform(List.of(tap));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Realiza un toque en un elemento específico.
	 * @param locator El locator del elemento en el que se realizará el toque.
	 */
	public void touchElement(String locator) {
		try {
			Point location = find(locator).getLocation();

			int x = location.getX() + 50;
			int y = location.getY() + 50;

			PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "FINGER");
			Sequence tap = new Sequence(finger, 1);

			tap.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), x, y));
			tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
			tap.addAction(new Pause(finger, Duration.ofMillis(0)));
			tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

			driver.perform(List.of(tap));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Navega hacia atrás en la aplicación móvil.
	 */
	public void navigateBack() {
		driver.navigate().back();
	}

	/**
	 * Borra el contenido de un elemento identificado por su locator.
	 * @param locator El locator del elemento utilizando XPath.
	 */
	public void clearElement(String locator) {
		try {
			find(locator).clear();
		} catch (NoSuchElementException e) {
			System.out.println("Elemento no encontrado: " + locator);
			throw new RuntimeException(e);
		} catch (ElementNotInteractableException e) {
			System.out.println("El elemento no es interactuable: " + locator);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Selecciona un elemento de un menú desplegable por su valor.
	 * @param locator El locator del elemento utilizando XPath.
	 * @param valueToSelect El valor del elemento que se desea seleccionar en el menú desplegable.
	 */
	public void selectFromDropdownByValue(String locator, String valueToSelect) {
		try {
			Select dropdown = new Select(find(locator));
			dropdown.selectByValue(valueToSelect);
		} catch (NoSuchElementException | StaleElementReferenceException e) {
			System.out.println("Error al seleccionar el elemento del menú desplegable por valor: " + e.getMessage());
		}
	}

	/**
	 * Selecciona un elemento de un menú desplegable por su índice.
	 * @param locator El locator del elemento utilizando XPath.
	 * @param valueToSelect El índice del elemento que se desea seleccionar en el menú desplegable.
	 */
	public void selectFromDropdownByIndex(String locator, int valueToSelect) {
		try {
			Select dropdown = new Select(find(locator));
			dropdown.selectByIndex(valueToSelect);
		} catch (NoSuchElementException | StaleElementReferenceException e) {
			System.out.println("Error al seleccionar el elemento del menú desplegable por índice: " + e.getMessage());
		}
	}

	/**
	 * Selecciona un elemento de un menú desplegable por su texto visible.
	 * @param locator El locator del elemento utilizando XPath.
	 * @param valueToSelect El texto visible del elemento que se desea seleccionar en el menú desplegable.
	 */
	public void selectFromDropdownByText(String locator, String valueToSelect) {
		try {
			Select dropdown = new Select(find(locator));
			dropdown.selectByVisibleText(valueToSelect);
		} catch (NoSuchElementException | StaleElementReferenceException e) {
			System.out.println("Error al seleccionar el elemento del menú desplegable por texto visible: " + e.getMessage());
		}
	}

	/**
	 * Obtiene el texto de un elemento identificado por su locator.
	 * @param locator El locator del elemento utilizando XPath.
	 * @return El texto del elemento o una cadena vacía si el texto es nulo.
	 */
	public String textFromElement(String locator) {
		try {
			WebElement element = find(locator);
			if (element != null) {
				String text = element.getText();
				return (text != null) ? text : "";
			} else {
				return "";
			}
		} catch (NoSuchElementException | StaleElementReferenceException e) {
			System.out.println("Error al leer el texto del elemento: " + e.getMessage());
			return "";
		}
	}

	/**
	 * Obtiene el valor del atributo 'content-desc' de un elemento.
	 * @param locator El localizador del elemento.
	 * @return El valor del atributo 'content-desc'.
	 * @throws NoSuchElementException Si el elemento no tiene un atributo 'content-desc' válido.
	 */
	public String textFromElementAttribute(String locator) throws NoSuchElementException {
		try {
			String contentDesc = find(locator).getAttribute("content-desc");
			if (contentDesc != null && !contentDesc.isEmpty()) {
				return contentDesc;
			} else {
				throw new NoSuchElementException("El elemento no tiene un atributo 'content-desc' válido.");
			}
		} catch (NoSuchElementException e) {
			System.out.println("Error al leer el atributo 'content-desc' del elemento: " + e.getMessage());
			throw new NoSuchElementException("El elemento no tiene un atributo 'content-desc' válido.");
		}
	}

	/**
	 * Metodo Sobrecargado
	 *
	 * Obtiene el valor del atributo pasado por parametro de un elemento.
	 * @param locator El localizador del elemento.
	 * @param attribute El nombre del atributo.
	 * @return El valor del atributo.
	 * @throws NoSuchElementException Si el elemento no tiene un atributo válido.
	 */
	public String textFromElementAttribute(String locator, String attribute) throws NoSuchElementException {
		try {
			String textAttribute = find(locator).getAttribute(attribute);
			if (textAttribute != null && !textAttribute.isEmpty()) {
				return textAttribute;
			} else {
				throw new NoSuchElementException("El elemento no tiene un atributo "+ attribute + " válido.");
			}
		} catch (NoSuchElementException e) {
			System.out.println("Error al leer el atributo "+ attribute + " del elemento: " + e.getMessage());
			throw new NoSuchElementException("El elemento no tiene un atributo "+ attribute + " válido.");
		}
	}

	/**
	 * Verifica si un elemento está habilitado.
	 * @param locator El localizador del elemento.
	 * @return true si el elemento está habilitado, false en caso contrario.
	 */
	public boolean elementEnabled(String locator) {
		try {
			return find(locator).isEnabled();
		} catch (NoSuchElementException e) {
			System.out.println("Error al verificar si el elemento está habilitado: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Verifica si un elemento está visible en la página.
	 * @param locator El localizador del elemento.
	 * @return true si el elemento está visible, false en caso contrario.
	 */
	public boolean elementIsDisplayed(String locator) {
		try {
			return find(locator).isDisplayed();
		} catch (NoSuchElementException | StaleElementReferenceException e) {
			System.out.println("Error al verificar si el elemento está visible: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Verifica si un elemento está seleccionado.
	 * @param locator El localizador del elemento.
	 * @return true si el elemento está seleccionado, false en caso contrario.
	 */
	public boolean elementIsSelected(String locator) {
		try {
			WebElement element = find(locator);
			return (element != null) && element.isSelected();
		} catch (NoSuchElementException | StaleElementReferenceException e) {
			System.out.println("Error al verificar si el elemento está seleccionado: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Verifica si el texto de un elemento está vacío.
	 * @param locator El localizador del elemento.
	 * @return true si el texto del elemento está vacío, false en caso contrario.
	 */
	public boolean isTextEmpty(String locator) {
		return textFromElement(locator).isEmpty();
	}

	/**
	 * Obtiene una lista de todos los elementos que coinciden con el localizador.
	 * @param locator El localizador de los elementos.
	 * @return La lista de elementos.
	 */
	public List<WebElement> bringMeAllElements(String locator) {
		return finds(locator);
	}

	/**
	 * Lee el texto de una lista de elementos y lo devuelve como una lista de cadenas.
	 * @param locator El localizador de los elementos.
	 * @return La lista de textos de los elementos.
	 */
	public List<String> readFromElements(String locator) {
		try {
			List<WebElement> elements = bringMeAllElements(locator);
			List<String> textList = new ArrayList<>();

			for (WebElement element : elements) {
				textList.add(element.getText());
			}

			return textList;
		} catch (Exception e) {
			System.out.println("Error al leer el texto de los elementos: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Lee el valor del atributo 'content-desc' de una lista de elementos y lo devuelve como una lista de cadenas.
	 * @param locator El localizador de los elementos.
	 * @return La lista de valores del atributo 'content-desc'.
	 */
	public List<String> readFromElementsAttribute(String locator) {
		try {
			List<WebElement> elements = bringMeAllElements(locator);
			List<String> contentDescList = new ArrayList<>();

			for (WebElement element : elements) {
				contentDescList.add(element.getAttribute("content-desc"));
			}

			return contentDescList;
		} catch (Exception e) {
			System.out.println("Error al leer el atributo 'content-desc' de los elementos: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Realiza una acción de clic y mantenimiento sobre un elemento.
	 * @param locator El localizador del elemento.
	 */
	public void clickAndHold(String locator, int milliseconds) {
		try {
			Actions actions = new Actions(driver);
			actions.moveToElement(find(locator));
			actions.clickAndHold().pause(milliseconds).release().perform();
		} catch (Exception e) {
			System.out.println("Error al hacer clic y mantener presionado el elemento: " + e.getMessage());
		}
	}

	/**
	 * Espera hasta que un elemento, identificado por un localizador XPath, esté visible en la página.
	 *
	 * @param locator El localizador XPath del elemento a esperar que sea visible.
	 * @throws TimeoutException si el elemento no está visible después del tiempo definido de espera.
	 */
	public void waitForElementToBeVisible(String locator) {
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
		} catch (TimeoutException e) {
			throw new TimeoutException("El elemento no está visible después del tiempo esperado: " + locator, e);
		}
	}

	/**
	 * Espera hasta que un elemento, identificado por un localizador XPath, no esté visible en la página.
	 *
	 * @param locator El localizador XPath del elemento a esperar que sea invisible.
	 * @throws TimeoutException si el elemento sigue visible después del tiempo definido de espera.
	 */
	public void waitForElementToBeInvisible(String locator) {
		try {
			wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(locator)));
		} catch (TimeoutException e) {
			throw new TimeoutException("El elemento sigue visible después del tiempo esperado: " + locator, e);
		}
	}
	/**
	 * Espera hasta que un elemento, identificado por un localizador XPath, sea clickable en la página.
	 *
	 * @param locator El localizador XPath del elemento a esperar que sea clickable.
	 * @throws TimeoutException si el elemento no se volvio clickable después del tiempo definido de espera.
	 */
	public void waitForElementToBeClickable(String locator) {
		try {
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath(locator)));
		} catch (TimeoutException e) {
			throw new TimeoutException("El elemento no se volvio clickable después del tiempo esperado: " + locator, e);
		}
	}

	public void touchMarginElement(String locator, String horizontal, String vertical) {
		try {
			WebElement element = driver.findElement(By.xpath(locator));
			int width = element.getRect().getWidth();
			int height = element.getRect().getHeight();

			Point location = find(locator).getLocation();

			int x = 0;
			int y = 0;

			//Eje Horizontal
			if(horizontal.equals("RIGHT")){ //derecha
				x = location.getX() + width;
			}
			if(horizontal.equals("LEFT")){ //izquierda
				x = location.getX();
			}
			if(horizontal.equals("CENTER")){ //centro x
				x = location.getX() + width / 2;
			}

			//Eje Vertical
			if(vertical.equals("TOP")){ //arriba
				y = location.getY();
			}
			if(vertical.equals("BOTTOM")){ //abajo
				y = location.getY() + height;
			}
			if(vertical.equals("CENTER")){//centro
				y = location.getY() + height / 2;
			}

			PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "FINGER");
			Sequence tap = new Sequence(finger, 1);

			tap.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), x, y));
			tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
			tap.addAction(new Pause(finger, Duration.ofMillis(0)));
			tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

			driver.perform(List.of(tap));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** Metodo Sobrecargado
	 *
	 * Toque en los bordes de un objeto * Medida de holgura + o - con respecto al borde
	 * marginx Horizontal y marginy Vertical
	 Sus parametos son Eje Horizontal donde las opciones posibles son:
	 RIGHT Derecha, LEFT izquierda, CENTER para el centro

	 Eje Vertical donde las opciones posibles son:
	 TOP Arriba, BOTTOM Abajo, CENTER para el centro
	 */
	public void touchMarginElement(String locator, String horizontal, String vertical, Integer marginx, Integer marginy) {
		try {
			WebElement element = driver.findElement(By.xpath(locator));
			int width = element.getRect().getWidth();
			int height = element.getRect().getHeight();

			Point location = find(locator).getLocation();

			int x = 0;
			int y = 0;

			//Eje Horizontal
			if(horizontal.equals("RIGHT")){ x = (location.getX() + width) + marginx;}
			if(horizontal.equals("LEFT")){ x = (location.getX()) + marginx;}
			if(horizontal.equals("CENTER")){ x = (location.getX() + width / 2) + marginx;}

			//Eje Vertical
			if(vertical.equals("TOP")){ y = (location.getY()) + marginy; }
			if(vertical.equals("BOTTOM")){ y = (location.getY() + height) + marginy; }
			if(vertical.equals("CENTER")){ y = (location.getY() + height / 2) + marginy; }

			PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "FINGER");
			Sequence tap = new Sequence(finger, 1);

			tap.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), x, y));
			tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
			tap.addAction(new Pause(finger, Duration.ofMillis(0)));
			tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

			driver.perform(List.of(tap));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
