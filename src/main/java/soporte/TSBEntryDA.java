package soporte;

import java.util.Map;

/**
 * Interface que adiciona operaciones a la interfaz Map.Entry proporcionada por
 * Java, necesarias para trabajar con Direccionamiento Abierto respetando la
 * programacion hacia las interfaces.
 */
public interface TSBEntryDA<K,V> extends Map.Entry<K, V>{


    /**
    * Operacion dedicada a realizar la eliminacion virtual del Entry
     * de forma que el mismo quede en un estado de Tumba, para facilitar
     * la busqueda de objetos u lugares disponibles
     */
    TSBEntryDA<K,V> delete();

    /**
     * Operacion que devuelve un booleano que indica si el objeto a sido eliminado
     * y se encuentra en estado de tumba
     */
    boolean isDeleted();
}
