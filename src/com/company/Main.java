package com.company;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

@SuppressWarnings("serial")
public class Main extends JFrame {
    // Начальные размеры окна приложения

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    // Объект диалогового окна для выбора файлов
    private JFileChooser fileChooser = null;
    // Пункты меню
    private JCheckBoxMenuItem showAxisMenuItem;
    private JCheckBoxMenuItem showMarkersMenuItem;
    // Компонент-отображатель графика
    private GraphicsDisplay display = new GraphicsDisplay();
    // Флаг, указывающий на загруженность данных графика
    private boolean fileLoaded = false;

    public Main() {
        // Вызов конструктора предка Frame
        super("Построение графиков функций на основе заранее подготовленных файлов");
        setSize(WIDTH, HEIGHT); // Установка размеров окна
        // Отцентрировать окно приложения на экране
        Toolkit kit = Toolkit.getDefaultToolkit();
        // Развѐртывание окна на весь экран
        setLocation((kit.getScreenSize().width - WIDTH)/2,(kit.getScreenSize().height - HEIGHT)/2);
        // Создать и установить полосу меню
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        // Добавить пункт меню "Файл"
        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);
        // Создать действие по открытию файла
        Action openGraphicsAction = new AbstractAction("Открыть файл с графиком") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION)
                    openGraphics(fileChooser.getSelectedFile());
            }
        };
        // Добавить соответствующий элемент меню
        fileMenu.add(openGraphicsAction);
        // Создать пункт меню "График"
        JMenu graphicsMenu = new JMenu("График");
        menuBar.add(graphicsMenu);
        // Создать действие для реакции на активацию элемента "Показывать оси координат"
        Action showAxisAction = new AbstractAction("Показывать оси координат") {
            public void actionPerformed(ActionEvent event) {
                display.setShowAxis(showAxisMenuItem.isSelected());
            }
        };
        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
        // Добавить соответствующий элемент в меню
        graphicsMenu.add(showAxisMenuItem);
        // Элемент по умолчанию включен (отмечен флажком)
        showAxisMenuItem.setSelected(true);
        // Повторить действия для элемента "Показывать маркеры точек"
        Action showMarkersAction = new AbstractAction("Показывать маркеры точек") {
            // по аналогии с showAxisMenuItem
            public void actionPerformed(ActionEvent event) {
                display.setShowMarkers(showMarkersMenuItem.isSelected());
            }
        };
        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
        graphicsMenu.add(showMarkersMenuItem);
        showMarkersMenuItem.setSelected(true);

        graphicsMenu.addMenuListener(new GraphicsMenuListener());
        getContentPane().add(display, BorderLayout.CENTER);
    }
    // Считывание данных графика из существующего файла
    protected void openGraphics(File selectedFile) {
        try { //Открыть поток чтения данных, связанный с входным файловым потоком
            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
            // зная объѐм данных в потоке ввода можно вычислить, сколько памяти нужно зарезервировать в массиве:
            // Всего байт в потоке - in.available() байт, размер числа Double - Double.SIZE бит, или Double.SIZE/8 байт,
            // так как числа записываются парами,  то число пар меньше в 2 раза.
            Double[][] graphicsData = new Double[in.available()/(Double.SIZE/8)/2][];
            int i = 0;
            while (in.available()>0) { //Цикл чтения данных (пока в потоке есть данные)
                Double x = in.readDouble();
                Double y = in.readDouble();
                graphicsData[i++] = new Double[] {x, y}; // прочитанная пара координат добавляется в массив
            }
            if (graphicsData != null && graphicsData.length > 0) { // имеется ли в списке в результате чтения хотя бы одна пара координат
                fileLoaded = true; // да - установка флаг загруженности данных
                display.showGraphics(graphicsData); // вызов метода отображения графика
            }
            in.close(); // закрытие входного потока
        } catch (FileNotFoundException ex) {
            // В случае исключительной ситуации типа "Файл не найден"
           // показать сообщение об ошибке
            JOptionPane.showMessageDialog(Main.this, "Указанный файл не найден", "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(Main.this, "Ошибка чтения координат точек из файла", "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
            return;
        }
    }

    public static void main(String[] args) {
        // Создать и показать экземпляр главного окна приложения
        Main frame = new Main();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    // Класс-слушатель событий, связанных с отображением меню
    private class GraphicsMenuListener implements MenuListener {
        // Обработчик, вызываемый перед показом меню
        public void menuSelected(MenuEvent e) {
            // Доступность или недоступность элементов меню "График" определяется загруженностью данных
            showAxisMenuItem.setEnabled(fileLoaded);
            showMarkersMenuItem.setEnabled(fileLoaded);
        }
        public void menuDeselected(MenuEvent e) {
            // Обработчик, вызываемый после того, как меню исчезло с экрана
        }

        public void menuCanceled(MenuEvent e) {
            // Обработчик, вызываемый в случае отмены выбора пункта меню (очень редкая ситуация)
        }
    }
}