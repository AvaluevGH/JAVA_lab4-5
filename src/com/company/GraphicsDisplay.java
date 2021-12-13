package com.company;
//класс отображения графика
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.LinkedList;
import javax.swing.*;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    // Список координат точек для построения графика
    private Double[][] graphicsData;

    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = true;

    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    // Используемый масштаб отображения
    private double scale;

    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private boolean turnGraph = false;
    private BasicStroke markerStroke;
    // различные шрифты отображения надписей
    private Font axisFont;

    public GraphicsDisplay() {
        // Цвет заднего фона области отображения - белый
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[] {10, 10, 10, 10, 10, 10, 30 , 30, 30,30,30,30}, 0.0f); // перо для рисования графика
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f); // перо для рисования осей координат
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f); // перо для рисования контуров маркеров
        axisFont = new Font("Serif", Font.CENTER_BASELINE, 36); // шрифт для подписей осей координат
    }
    // Данный метод вызывается из обработчика элемента меню "Открыть файл с
    //графиком" главного окна приложения в случае успешной загрузки данных
    public void showGraphics(Double[][] graphicsData) {
        // Сохранить массив точек во внутреннем поле класса
        this.graphicsData = graphicsData;
        // Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
        repaint();
    }
    public void setShowAxis(boolean showAxis) {
        // Методы-модификаторы для изменения параметров отображения графика
        // Изменение любого параметра приводит к перерисовке области

        this.showAxis = showAxis;
        repaint();
    }
    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }
    @Override

    // Метод отображения всего компонента, содержащего график
    public void paintComponent (Graphics g) {
        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return; // если данные графика не загружены - ничего не делать
        minX = graphicsData[0][0];// определение минимального и максимального значения для координат X и Y, это необходимо для
        // определения  области пространства,подлежащей отображению, её верхний левый угол это (minX, maxY) - правый нижний это
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;
        // Найти минимальное и максимальное значение функции
        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1] > maxY) {
                maxY = graphicsData[i][1];
            }
        }
        //Определить (исходя из размеров окна) масштабы по осям X
        //и Y - сколько пикселов приходится на единицу длины по X и по Y
        if (!turnGraph) {
            double scaleX = getSize().getWidth() / (maxX - minX);
            double scaleY = getSize().getHeight() / (maxY - minY);
            //Чтобы изображение было неискажѐнным - масштаб должен
            //быть одинаков выбираем за основу минимальный
            scale = Math.min(scaleX, scaleY);
            if (scale == scaleX) { // корректировка границ отображаемой области согласно выбранному масштабу
                double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
                maxY += yIncrement;
                minY -= yIncrement;
            }
            if (scale == scaleY) {
                double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 4;
                maxX += xIncrement;
                minX -= xIncrement;
            }
        }

        Graphics2D canvas = (Graphics2D) g; // сохранение текущих настройки холста
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();

        // Первыми (если нужно) отрисовываются оси координат.
        if (showAxis) paintAxis(canvas);
        // Затем отображается сам график
        paintGraphics(canvas);
        // Затем (если нужно) отображаются маркеры точек, по которым строился график.
        if (showMarkers) paintMarkers(canvas);

        canvas.setFont(oldFont); // восстановление старых настроек холста
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    protected void paintGraphics(Graphics2D canvas) { // отрисовка графика по прочитанным координатам.
        canvas.setStroke(graphicsStroke); // выбрать линию для рисования графика.
        canvas.setColor(Color.GREEN); //цвет
        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]); // преобразование значения x, y в точку на экране point
            if (i > 0) { // не первая итерация цикла - вести линию в точку point
                graphics.lineTo(point.getX(), point.getY());
            } else { // первая итерация цикла - установить начало пути в точку point
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graphics); // отображение графика
    }
    // Отображение маркеров точек, по которым рисовался график
    protected void paintMarkers(Graphics2D canvas) {
        //Установить специальное перо для черчения контуров маркеров
        canvas.setStroke(markerStroke);
        // Выбрать красный цвета для контуров маркеров
        canvas.setColor(Color.RED);
        // Выбрать желтый цвет для закрашивания маркеров внутри
        canvas.setPaint(Color.YELLOW);
        //Организовать цикл по всем точкам графика
        for (Double[] point : graphicsData) {
            int size = 5;
            Ellipse2D.Double marker = new Ellipse2D.Double(); // инициализация эллипса как объект для представления маркера
            Point2D.Double center = xyToPoint(point[0], point[1]);
            Point2D.Double corner = shiftPoint(center, size, size);
            marker.setFrameFromCenter(center, corner);
            canvas.draw(marker); // Начертить контур маркера
            canvas.fill(marker); // Залить внутреннюю область маркера

            Line2D.Double line = new Line2D.Double(shiftPoint(center, -size, 0), shiftPoint(center, size, 0));
            Boolean highervalue = true;
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance();
            formatter.setMaximumFractionDigits(2);
            DecimalFormatSymbols dottedDouble = formatter.getDecimalFormatSymbols();
            dottedDouble.setDecimalSeparator('.');
            formatter.setDecimalFormatSymbols(dottedDouble);
            String temp = formatter.format(Math.abs(point[1]));
            temp = temp.replace(".", "");
            for (int i = 0; i < temp.length() - 1; i++) {
                if (temp.charAt(i) != 46 && (int) temp.charAt(i) > (int) temp.charAt(i + 1)) {
                    highervalue = false;
                    break;
                }
            }
            if (highervalue) {
                canvas.setColor(Color.BLACK);
            }
            canvas.draw(line);
            line.setLine(shiftPoint(center, 0, -size), shiftPoint(center, 0, size));
            canvas.draw(line);
            canvas.draw(marker);
            canvas.setColor(Color.BLUE);
        }
    }

    // Метод, обеспечивающий отображение осей координат
    protected void paintAxis(Graphics2D canvas) {
        // Установить особое начертание для осей
        canvas.setStroke(axisStroke);
        // Оси рисуются чѐрным цветом
        canvas.setColor(Color.BLACK);
        // Стрелки заливаются чѐрным цветом
        canvas.setPaint(Color.BLACK);
        // Подписи к координатным осям делаются специальным шрифтом
        canvas.setFont(axisFont);
        // Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
        FontRenderContext context = canvas.getFontRenderContext();
        // Определить, должна ли быть видна ось Y на графике
        if (minX <= 0.0 && maxX >= 0.0) {
            // Она должна быть видна, если левая граница показываемой
            //области (minX) <= 0.0, а правая (maxX) >= 0.0
            // Сама ось - это линия между точками (0, maxY) и (0, minY)
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
            // Стрелка оси Y
            GeneralPath arrow = new GeneralPath();
            // Установить начальную точку ломаной точно на верхний конец оси Y
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());

            // Вести левый "скат" стрелки в точку с относительнымикоординатами (5,20)
            arrow.lineTo(arrow.getCurrentPoint().getX()+5, arrow.getCurrentPoint().getY()+20);
            // Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
            arrow.lineTo(arrow.getCurrentPoint().getX()-10, arrow.getCurrentPoint().getY());
            // Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); //нарисовать стрелку
            canvas.fill(arrow); //закрашиваем
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);

            // Вывести надпись в точке с вычисленными координатами
            canvas.drawString("y", (float)labelPos.getX() + 10, (float)(labelPos.getY() - bounds.getY()));
            Rectangle2D centerBounds = axisFont.getStringBounds("0", context);
            Point2D.Double centerLabelPos = xyToPoint(0, 0);
            canvas.drawString("0", (float)centerLabelPos.getX() + 10, (float)(centerLabelPos.getY() - centerBounds.getY()));
        }
        // Определить, должна ли быть видна ось X на графике
        if (minY <= 0.0 && maxY >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX()-20, arrow.getCurrentPoint().getY()-5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY()+10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
            canvas.drawString("x", (float)(labelPos.getX() - bounds.getWidth() - 10), (float)(labelPos.getY() + bounds.getY()));
        }
    }
    protected Point2D.Double xyToPoint(double x, double y) {
        // Вычисляем смещение X от самой левой точки (minX)
        double deltaX = x - minX;
        // Вычисляем смещение Y от точки верхней точки (maxY)
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX*scale, deltaY*scale);
    }

    // Метод-помощник, возвращающий экземпляр класса Point2D.Double
     // смещѐнный по отношению к исходному на deltaX, deltaY


    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        // Инициализировать новый экземпляр точки
        Point2D.Double dest = new Point2D.Double();
        // Задать еѐ координаты как координаты существующей точки и заданные смещения
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }
}