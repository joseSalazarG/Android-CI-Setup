package steps;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.cucumber.java.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
import io.cucumber.java.Scenario;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

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
	private static final String APP_PACKAGE = "com.dsemu.drastic";
	private static final String APP_ACTIVITY = "com.dsemu.drastic.DraSticActivity";
	private static final String APP_PATH = "src/test/resources/app/drastic.apk";
	private static final String APPIUM_SERVER_URL = "http://127.0.0.1:4723/";

	//API Endpoints
	public static final String urlToken = "https://xxxxxxx/hubapi/login";
	public static final String credenciales = "{\"email\":\"xxxxx\",\"password\":\"xxxxxxx\"}";
	public static final String authToken = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
	public static String hubToken = null;

	/**
	 * Configura el entorno antes de ejecutar las pruebas.
	 * Crea las capacidades, inicializa el driver de Appium y registra un mensaje informativo.
	 */
	@Before
	public void setUp() {
		log.info("Iniciando el servidor de Appium");
		appiumServer = AppiumDriverLocalService.buildDefaultService();
		appiumServer.start();
		appiumServer.clearOutPutStreams();
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
			} finally {
				appiumServer.stop();
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
			log.error("Error al cargar el archivo: " + e.getMessage());
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
	 * Genera un String Random
	 */
	public static String generateRandomString(int lenght) {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuilder stringBuilder = new StringBuilder(lenght);

		for (int i = 0; i < lenght; i++) {
			int index = random.nextInt(characters.length());
			stringBuilder.append(characters.charAt(index));
		}

		return stringBuilder.toString();
	}
	public static String generateRandomNumber(int n) {
		Random random = new Random();
		int numeroAleatorio = random.nextInt(n);
		return String.format("%06d", numeroAleatorio);
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
			if(horizontal.equals("RIGHT")){ //derecha
				x = (location.getX() + width) + marginx;
			}
			if(horizontal.equals("LEFT")){ //izquierda
				x = (location.getX()) + marginx;
			}
			if(horizontal.equals("CENTER")){ //centro x
				x = (location.getX() + width / 2) + marginx;
			}

			//Eje Vertical
			if(vertical.equals("TOP")){ //arriba
				y = (location.getY()) + marginy;
			}
			if(vertical.equals("BOTTOM")){ //abajo
				y = (location.getY() + height) + marginy;
			}
			if(vertical.equals("CENTER")){//centro
				y = (location.getY() + height / 2) + marginy;
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
	/**
	 * Obtiene el token de autenticación para realizar solicitudes en la aplicación.
	 * @param baseUrl El endpoint a la api para obtener el token.
	 * @param jsonCredenciales las credenciales para conectarse.
	 * @param authorization token de autorizacion para acceder a la api.
	 * @return el token de autenticación.
	 */
	public String getHubToken(String baseUrl, String jsonCredenciales ,String authorization ) {

		RequestSpecification request = RestAssured.given();
		request.header("Authorization", authorization);
		request.contentType(ContentType.JSON);
		request.body(jsonCredenciales);

		Response response = request.post(baseUrl);
		int statusCode = response.getStatusCode();
		String cachedToken = null;

		if (statusCode == 200) {
			cachedToken = response.jsonPath().getString("data.token");
			System.err.println(cachedToken);
		}
		return cachedToken;
	}
	/**
	 * Busca la ultima solicitud en la aplicación segun la id del usuario  y devuelve su identificador.
	 * @param user el numero de id del usuario.
	 * @return El identificador del usuario.
	 * @throws IllegalStateException Si no se encuentran usuarios en la respuesta.
	 */
	public String searchRequestId(String user) {
		String url = "https://xxxxxxxxxxx/hubapi/members?search=";
		String searchUrl = url + user;

		hubToken = getHubToken(urlToken,credenciales, authToken);

		RequestSpecification request = RestAssured.given();
		request.header("Authorization", hubToken);
		request.contentType(ContentType.JSON);

		Response response = request.get(searchUrl);
		List<String> userIds = response.jsonPath().getList("data.data._id");

		if (!userIds.isEmpty()) {

			return userIds.get(0);
		} else {
			throw new IllegalStateException("No se encontraron solicitudes en la respuesta.");
		}
	}


	/**
	 * Busca la ultima solicitud en la aplicación segun la cedula del usuario  y devuelve su tipo.
	 * @param user el numero de cedula del usuario.
	 * @return El tipo de solicitud.
	 * @throws IllegalStateException Si no se encuentra el tipo de las solicitudes en la respuesta.
	 */
	public String searchRequestChangeType(String user) {
		String url = "https://xxxxxxxxxxx/api/dev/userstreamchange/list_get?search=";
		String baseUrl = url + user;

		hubToken = getHubToken(urlToken,credenciales, authToken);

		RequestSpecification request = RestAssured.given();
		request.header("Authorization", hubToken);
		request.contentType(ContentType.JSON);

		Response response = request.get(baseUrl);
		List<String> solicitudChangeTypes = response.jsonPath().getList("data.data.changeType");

		if (!solicitudChangeTypes.isEmpty()) {

			return solicitudChangeTypes.get(0);
		} else {
			throw new IllegalStateException("No se encontraron solicitudes en la respuesta.");
		}
	}

	/**
	 * Cambia el estado de una solicitud en la aplicación segun la cedula del usuario  y su tipo.
	 * @param user el numero de cedula del usuario.
	 * @param status el estado al cual se quiere cambiar la solicitud nueva.
	 * @throws IllegalStateException Si no se encuentra el tipo de las solicitudes en la respuesta.
	 */
	public void putChangeStatus(String user, String status) {
		String baseUrl = "https://xxxxxxxxxxx/api/dev/userstreamchange/status";
		String id = searchRequestId(user);
		String changeType = searchRequestChangeType(user);
		String json= null;

		if(Objects.equals(changeType, "NEW")){
			json = "{ \"_id\": \"" + id + "\",\"status\": \"" + status + "\",\"statusReason\": \"Motivo de prueba\" }";

		} else if (Objects.equals(changeType, "DELETE")) {
			json = "{ \"_id\": \"" + id + "\",\"status\": \"ACEPTADO\",\"statusReason\": \"Motivo de prueba\" }";
		}

		hubToken = getHubToken(urlToken,credenciales, authToken);

		RequestSpecification request = RestAssured.given();
		request.header("Authorization", hubToken);
		request.contentType(ContentType.JSON);
		request.body(json);

		Response response = request.put(baseUrl);
		int statusCode = response.getStatusCode();


		if (statusCode == 200) {
			//cachedToken = response.jsonPath().getString("data");

		} else {
			throw new IllegalStateException("No se encontraron solicitudes que modificar en la respuesta.");
		}
	}

	//Busca un reporte para su posterior eliminacion
	public String searchReporte(String idreporte) {
		String url = "https://xxxxxxxxxxx/api/dev/report/list?search=";
		String baseUrl = url + idreporte;

		hubToken = getHubToken(urlToken,credenciales, authToken);

		RequestSpecification request = RestAssured.given();
		request.header("Authorization", hubToken);
		request.contentType(ContentType.JSON);

		Response response = request.get(baseUrl);
		List<String> solicitudId = response.jsonPath().getList("data.data._id");

		if (!solicitudId.isEmpty()) {
			return solicitudId.get(0);
		} else {
			throw new IllegalStateException("No se encontro la id del reporte");
		}
	}

	//Elimino el reporte
	public void eliminarReporteHub(String idReporte) {
		String baseUrl = "https://xxxxxxxxxxx/api/dev/report/";
		String id = null;
		String req = null;

		id = searchReporte(idReporte);
		req = baseUrl + id + "/remove";

		hubToken = getHubToken(urlToken,credenciales, authToken);

		RequestSpecification request = RestAssured.given();
		request.header("Authorization", hubToken);

		Response response = request.delete(req);
		int statusCode = response.getStatusCode();

		if (statusCode == 200) {
			log.info("Fue eliminado el reporte");
		} else {
			throw new IllegalStateException("No se pudo aprobar eliminar el reporte");
		}
	}
}