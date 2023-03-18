package ReadCom;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Enumeration;

public class SerialPortToolKit {
    public  Double readPort() throws Exception {
        DecimalFormat df = new DecimalFormat("#.#");
        // Найти идентификатор порта
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier("COM3");

        // Открыть порт и получить объект SerialPort
        SerialPort serialPort = (SerialPort) portIdentifier.open("SerialReader", 2000);

        // Установить параметры порта
        serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // Получить поток ввода
        InputStream inputStream = serialPort.getInputStream();

        // Читать 3 байта из потока ввода
        byte[] buffer = new byte[3];
        inputStream.read(buffer);
        if (buffer [1] == 0 && buffer [2] == 0) {
            // если информация неполная, то пропускаем такой пакет
            inputStream.close();
            serialPort.close();
            return 0.0;
        } else {
            // Обработать байты, чтобы получить нужную информацию
            // последний бит первого байта
            // весь второй байт
            // первый бит третьего байта
            // вывести полученное значение
            //System.out.println("Value of voltage: " + df.format (3.3*value/1024));
            // Закрыть поток ввода и порт
            inputStream.close();
            serialPort.close();
            System.out.println("Close port");
            return (double) (((buffer[0] & 0x01) << 9) | ((buffer[1] & 0xFF) << 1) | ((buffer[2] & 0x80) >> 7));
        }


    }
    public void sendBytes(byte[] bytesToSend) {
        String portName = "COM3"; // название порта

        CommPortIdentifier portIdentifier = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        // перебор доступных портов для нахождения нужного
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortIdentifier = (CommPortIdentifier) portEnum.nextElement();
            if (currPortIdentifier.getName().equals(portName)) {
                portIdentifier = currPortIdentifier;
                break;
            }
        }

        // проверка наличия порта
        if (portIdentifier == null) {
            System.err.println("Could not find COM port.");
            return;
        }

        try {
            // открытие порта и настройка параметров
            SerialPort serialPort = (SerialPort) portIdentifier.open("SerialWriter", 2000);
            serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            // отправка данных
            OutputStream outputStream = serialPort.getOutputStream();
            outputStream.write(bytesToSend);

            // закрытие порта
            outputStream.close();
            serialPort.close();
            System.out.println("Close port");
            // msg sent
            //System.out.println("Data sent: " + Arrays.toString(bytesToSend));

        } catch (PortInUseException | UnsupportedCommOperationException | IOException e) {
            //System.err.println("Error: " + e.getMessage());
        }
    }

}
