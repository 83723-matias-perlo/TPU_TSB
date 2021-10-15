package soporte;

import java.io.Serializable;
import java.util.*;

public class TSBHashTableDA<K,V>  implements Map<K,V>, Cloneable, Serializable
{
    private final static int MAX_SIZE = Integer.MAX_VALUE;
    private TSBEntryDA<K,V>[] table;
    private int initial_capacity;
    private int count;
    private float load_factor = 0.5f;
    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K,V>> entrySet = null;
    private transient Collection<V> values = null;
    protected transient int modCount;

    public TSBHashTableDA()
    {
        this(11);
    }

    public TSBHashTableDA(int initial_capacity)
    {
        if(initial_capacity <= 0) { initial_capacity = 11; }
        else
        {
            if(initial_capacity > TSBHashTableDA.MAX_SIZE)
            {
                if (!esPrimo(TSBHashTableDA.MAX_SIZE))
                    initial_capacity = primoAnterior(TSBHashTableDA.MAX_SIZE);
                else
                    initial_capacity = TSBHashTableDA.MAX_SIZE;
            }
            else {
                if(!esPrimo(initial_capacity)) {
                    int tempCapacity = primoSiguiente(initial_capacity);
                    if (tempCapacity != -1)
                        initial_capacity = tempCapacity;
                    else
                        initial_capacity = primoAnterior(initial_capacity);
                }
            }
        }
        this.table = new Entry[initial_capacity];
        this.initial_capacity = initial_capacity;
        this.count = 0;
        this.modCount = 0;
    }

    public TSBHashTableDA(Map<? extends K,? extends V> t)
    {
        this(11);
        this.putAll(t);
    }

    private int primoAnterior(int initialNumber) {
        int count = initialNumber;
        if (count % 2 == 0) count--;
        while(count > 0 && !esPrimo(count)){
            count-=2;
        }
        if(esPrimo(count)) return count;
        return -1;
    }

    private int primoSiguiente(int initialNumber){
        int count = initialNumber;
        if (count % 2 == 0) count++;
        while(count < TSBHashTableDA.MAX_SIZE && !esPrimo(count)){
            count+=2;
        }
        if(esPrimo(count)) return count;
        return -1;
    }

    private boolean esPrimo(int n) {
        if(n <= 0) return false;
        if(n == 1) return true;

        int sqrtN = (int)Math.sqrt((double)n);
        for (int i = 3; i < sqrtN; i+=2) {
            if(n % i == 0) return false;
        }
        return true;
    }

    @Override
    public int size()
    {
        return this.count;
    }

    @Override
    public boolean isEmpty()
    {
        return (this.count == 0);
    }

    @Override
    public boolean containsKey(Object key)
    {
        return (this.get((K)key) != null);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return this.contains(value);
    }

    @Override
    public V get(Object key)
    {
        if(key == null) throw new NullPointerException("get(): parámetro null");
        int ib = getKeyIndex(key);
        return (ib != -1)? (V)(this.table[ib]).getValue() : null;
    }

    @Override
    public V put(K key, V valor)
    {
        if(key == null || valor == null) throw new NullPointerException("put(): parámetro null");

        int ib = getPutPlace(key);
        if (ib == -1) return null;

        TSBEntryDA<K, V> entry = this.table[ib];
        V old = null;
        if(entry != null)
        {
            old = (V)entry.getValue();
            entry.setValue(valor);
        }
        else {
            if(this.count > this.load_factor * this.table.length) {
                this.rehash();
                ib = getPutPlace(key);
            }
            this.table[ib] = new Entry<K, V>(key, valor);
            this.count++;
            this.modCount++;
        }

        return old;
    }

    @Override
    public V remove(Object key)
    {
        if(key == null) throw new NullPointerException("remove(): parámetro null");

        int ib = getKeyIndex(key);
        if (ib == -1) return null;

        TSBEntryDA<K,V> entry = this.table[ib];
        entry.delete();

        // Cambiamos las variables para registrar el nuevo cambio realizado
        this.count--;
        this.modCount++;

        return (V)entry.getValue();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        for(Map.Entry<? extends K, ? extends V> e : m.entrySet())
        {
            put(e.getKey(), e.getValue());
        }
    }

    private int getKeyIndex(Object key) {
        int ib = this.h(key.hashCode()), j = 1;
        TSBEntryDA<K,V> entry = this.table[ib];

        while (entry != null && ( entry.getKey() != key || entry.isDeleted())){
            ib = (ib + (int)Math.pow(j, 2)) % this.table.length;
            entry = this.table[ib];
            j++;
        }
        if (entry != null && !entry.isDeleted()){
            return ib;
        }
        else {
            return -1;
        }
    }

    private int getPutPlace(Object key){
        int ib = this.h(key.hashCode()), j = 1, primerTumba = -1;
        boolean isPrimerTumba = true;
        TSBEntryDA<K,V> entry = this.table[ib];

        while (entry != null && !entry.getKey().equals(key)){
            if (entry.isDeleted() && isPrimerTumba){
                primerTumba = ib;
                isPrimerTumba = false;
            }
            ib = (ib + (int)Math.pow(j, 2)) % this.table.length;
            entry = this.table[ib];
//            if(entry == null || entry.getKey().equals(key)) break;

            j++;
        }
        if (entry == null && !isPrimerTumba) return primerTumba;
        return ib;
    }

    @Override
    public void clear()
    {
        this.table = new Entry[this.initial_capacity];
        this.count = 0;
        this.modCount++;
    }

    @Override
    public Set<K> keySet()
    {
        if(keySet == null)
        {
            // keySet = Collections.synchronizedSet(new KeySet());
            keySet = new KeySet();
        }
        return keySet;
    }

    @Override
    public Collection<V> values()
    {
        if(values==null)
        {
            // values = Collections.synchronizedCollection(new ValueCollection());
            values = new ValueCollection();
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        if(entrySet == null)
        {
            // entrySet = Collections.synchronizedSet(new EntrySet());
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        TSBHashTableDA<K, V> t = (TSBHashTableDA<K, V>)super.clone();
        t.table = new Entry[table.length];
        for (int i = table.length ; i-- > 0 ; ) t.table[i] = table[i];

        t.keySet = null;
        t.entrySet = null;
        t.values = null;
        t.modCount = 0;
        return t;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Map)) { return false; }

        Map<K, V> t = (Map<K, V>) obj;
        if(t.size() != this.size()) { return false; }

        try
        {
            Iterator<Map.Entry<K,V>> i = this.entrySet().iterator();
            while(i.hasNext())
            {
                Map.Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if(t.get(key) == null) { return false; }
                else
                {
                    if(!value.equals(t.get(key))) { return false; }
                }
            }
        }

        catch (ClassCastException | NullPointerException e)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        if(this.isEmpty()) {return 0;}

        int hc = 0;
        for(Map.Entry<K, V> entry : this.entrySet())
        {
            hc += entry.hashCode();
        }

        return hc;
    }

    @Override
    public String toString()
    {
        StringBuilder cad = new StringBuilder("");
        for(int i = 0; i < this.table.length; i++)
        {
            cad.append("\nEntrada "+ i + ":\t" + this.table[i].toString());
        }
        return cad.toString();
    }


    //************************ Métodos específicos de la clase.

    public boolean contains(Object value)
    {
        if(value == null) return false;
        for(TSBEntryDA<K, V> entry : this.table) {
            if (entry != null && value.equals(entry.getValue()) && !entry.isDeleted()) {
                return true;
            }
        }
        return false;
    }

    protected void rehash()
    {
        int old_length = this.table.length;
        int new_length = primoSiguiente(old_length * 2 + 1);
        if (new_length == -1) new_length = primoAnterior(old_length * 2 + 1);
        TSBEntryDA<K,V> temp[] = new Entry[new_length];

        this.modCount++;

        for(int i = 0; i < this.table.length; i++)
        {
            TSBEntryDA<K,V> x = this.table[i];
            if(x != null){
                int y = this.h(x.getKey().hashCode(), temp.length);
                temp[y] = x;
            }
        }
        this.table = temp;
    }

    private int h(int k)
    {
        return h(k, this.table.length);
    }
    private int h(K key)
    {
        return h(key.hashCode(), this.table.length);
    }
    private int h(K key, int t)
    {
        return h(key.hashCode(), t);
    }
    private int h(int k, int t)
    {
        if(k < 0) k *= -1;
        return k % t;
    }

    private Map.Entry<K, V> search_for_entry(K key, TSBArrayList<Map.Entry<K, V>> bucket)
    {
        Iterator<Map.Entry<K, V>> it = bucket.iterator();
        while(it.hasNext())
        {
            Map.Entry<K, V> entry = it.next();
            if(key.equals(entry.getKey())) return entry;
        }
        return null;
    }

    private int search_for_index(K key, TSBArrayList<Map.Entry<K, V>> bucket)
    {
        Iterator<Map.Entry<K, V>> it = bucket.iterator();
        for(int i=0; it.hasNext(); i++)
        {
            Map.Entry<K, V> entry = it.next();
            if(key.equals(entry.getKey())) return i;
        }
        return -1;
    }

    private class Entry<K, V> implements TSBEntryDA<K, V>
    {
        private K key;
        private V value;
        private boolean isDeleted;

        public Entry(K key, V value)
        {
            if(key == null || value == null)
            {
                throw new IllegalArgumentException("Entry(): parámetro null...");
            }
            this.key = key;
            this.value = value;
            this.isDeleted = false;
        }

        @Override
        public K getKey()
        {
            return key;
        }

        @Override
        public V getValue()
        {
            return value;
        }

        public boolean isDeleted() {
            return isDeleted;
        }

        @Override
        public V setValue(V value)
        {
            if(value == null)
            {
                throw new IllegalArgumentException("setValue(): parámetro null...");
            }

            V old = this.value;
            this.value = value;
            return old;
        }

        public Entry delete(){
            this.isDeleted = true;
            return this;
        }

        @Override
        public int hashCode()
        {
            // PODRIAMOS CAMBIARLO
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (this.getClass() != obj.getClass()) { return false; }

            final Entry other = (Entry) obj;
            if (!Objects.equals(this.key, other.key)) { return false; }
            if (!Objects.equals(this.value, other.value)) { return false; }
            return true;
        }

        @Override
        public String toString()
        {
            return "(" + key.toString() + ", " + value.toString() + ")";
        }
    }

    private class KeySet extends AbstractSet<K> implements Iterable<K>
    {
        @Override
        public Iterator<K> iterator()
        {
            return new KeySetIterator();
        }

        @Override
        public int size()
        {
            return TSBHashTableDA.this.count;
        }

        @Override
        public boolean contains(Object o)
        {
            return TSBHashTableDA.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o)
        {
            return (TSBHashTableDA.this.remove(o) != null);
        }

        @Override
        public void clear()
        {
            TSBHashTableDA.this.clear();
        }

        private class KeySetIterator implements Iterator<K>
        {

            private int indice;
            private int current_entry;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public KeySetIterator()
            {
                indice = 0;
                current_entry = 0;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext()
            {
                // variable auxiliar t para simplificar accesos...
//                TSBArrayList<Map.Entry<K, V>> t = TSBHashTableDA.this.table;
                TSBEntryDA<K,V>[] t = TSBHashTableDA.this.table;

//                if(TSBHashTableDA.this.isEmpty()) { return false; }
                if(indice >= t.length) { return false; }

                // bucket actual vacío o listo?...
                if(t[indice] == null)
                {
                    while(indice < t.length && (t[indice] == null || t[indice].isDeleted()))
                    {
                        indice++;
                    }
                    if(indice >= t.length) { return false; }
                }

                // en principio alcanza con esto... revisar...
                return true;
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public K next()
            {
                // control: fail-fast iterator...
                if(TSBHashTableDA.this.modCount != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                TSBEntryDA<K,V>[] t = TSBHashTableDA.this.table;

                while(t[indice] == null || t[indice].isDeleted()) indice++;
                Map.Entry<K, V> entry = t[indice];

                indice++;

                return entry.getKey();

            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove()
            {
                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                // eliminar el objeto que retornó next() la última vez...
                TSBEntryDA<K,V> garbage = TSBHashTableDA.this.table[indice].delete();

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // la tabla tiene un elementon menos...
                TSBHashTableDA.this.count--;

                // fail_fast iterator: todo en orden...
                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }

    /*
     * Clase interna que representa una vista de todos los PARES mapeados en la
     * tabla: si la vista cambia, cambia también la tabla que le da respaldo, y
     * viceversa. La vista es stateless: no mantiene estado alguno (es decir, no
     * contiene datos ella misma, sino que accede y gestiona directamente datos
     * de otra fuente), por lo que no tiene atributos y sus métodos gestionan en
     * forma directa el contenido de la tabla. Están soportados los metodos para
     * eliminar un objeto (remove()), eliminar todo el contenido (clear) y la
     * creación de un Iterator (que incluye el método Iterator.remove()).
     */
    private class EntrySet extends AbstractSet<Map.Entry<K, V>> implements Iterable<Map.Entry<K, V>>
    {

        @Override
        public Iterator<Map.Entry<K, V>> iterator()
        {
            return new EntrySetIterator();
        }

        /*
         * Verifica si esta vista (y por lo tanto la tabla) contiene al par
         * que entra como parámetro (que debe ser de la clase Entry).
         */
        @Override
        public boolean contains(Object o)
        {
            if(o == null) { return false; }
            if(!(o instanceof Entry)) { return false; }

            Map.Entry<K, V> entry = (Map.Entry<K,V>)o;
            int index = TSBHashTableDA.this.getKeyIndex(entry.getKey());
            if(index == -1) return false;
            return TSBHashTableDA.this.table[index].equals(entry);
        }

        /*
         * Elimina de esta vista (y por lo tanto de la tabla) al par que entra
         * como parámetro (y que debe ser de tipo Entry).
         */
        @Override
        public boolean remove(Object o)
        {
            if(o == null) { throw new NullPointerException("remove(): parámetro null");}
            if(!(o instanceof Entry)) { return false; }

            TSBEntryDA<K,V> entry = (TSBEntryDA<K,V>) o;
            int index = TSBHashTableDA.this.getKeyIndex(entry.getKey());
            if(index == -1) return false;

            TSBEntryDA<K,V> tempEntry = TSBHashTableDA.this.table[index];
            if(tempEntry.equals(entry)) {
                tempEntry.delete();
                TSBHashTableDA.this.count--;
                TSBHashTableDA.this.modCount++;
                return true;
            }
            return false;
        }

        @Override
        public int size()
        {
            return TSBHashTableDA.this.count;
        }

        @Override
        public void clear()
        {
            TSBHashTableDA.this.clear();
        }

        private class EntrySetIterator implements Iterator<Map.Entry<K, V>>
        {

            // índice del elemento actual en el iterador (el que fue retornado
            // la última vez por next() y será eliminado por remove())...
            private int current_entry;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public EntrySetIterator()
            {
                current_entry = 0;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext()
            {
                // variable auxiliar t para simplificar accesos...
                TSBEntryDA<K, V>[] t = TSBHashTableDA.this.table;

                if(TSBHashTableDA.this.isEmpty()) { return false; }
                if(TSBHashTableDA.this.count >= t.length) { return false; }

                while(current_entry < t.length && (t[current_entry] == null || t[current_entry].isDeleted()))
                {
                    current_entry++;
                }
                if(current_entry >= t.length) return false;
                return !t[current_entry].isDeleted();
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public Map.Entry<K, V> next()
            {
                // control: fail-fast iterator...
                if(TSBHashTableDA.this.modCount != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                TSBEntryDA<K, V> t = TSBHashTableDA.this.table[current_entry];
                current_entry++;
                next_ok = true;
                return t;
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove()
            {
                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                TSBEntryDA<K, V> garbage = TSBHashTableDA.this.table[current_entry];
                garbage.delete();

                next_ok = false;
                TSBHashTableDA.this.count--;
                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }

    /*
     * Clase interna que representa una vista de todos los VALORES mapeados en
     * la tabla: si la vista cambia, cambia también la tabla que le da respaldo,
     * y viceversa. La vista es stateless: no mantiene estado alguno (es decir,
     * no contiene datos ella misma, sino que accede y gestiona directamente los
     * de otra fuente), por lo que no tiene atributos y sus métodos gestionan en
     * forma directa el contenido de la tabla. Están soportados los metodos para
     * eliminar un objeto (remove()), eliminar todo el contenido (clear) y la
     * creación de un Iterator (que incluye el método Iterator.remove()).
     */
    private class ValueCollection extends AbstractCollection<V> implements Iterable<V>
    {
        @Override
        public Iterator<V> iterator()
        {
            return new ValueCollectionIterator();
        }

        @Override
        public int size()
        {
            return TSBHashTableDA.this.count;
        }

        @Override
        public boolean contains(Object o)
        {
            return TSBHashTableDA.this.containsValue(o);
        }

        @Override
        public void clear()
        {
            TSBHashTableDA.this.clear();
        }

        private class ValueCollectionIterator implements Iterator<V>
        {
            // índice del elemento actual en el iterador (el que fue retornado
            // la última vez por next() y será eliminado por remove())...
            private int current_entry;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public ValueCollectionIterator()
            {
                current_entry = 0;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext()
            {

                // variable auxiliar t para simplificar accesos...
                TSBEntryDA<K, V>[] t = TSBHashTableDA.this.table;

                if(TSBHashTableDA.this.isEmpty()) { return false; }
                if(TSBHashTableDA.this.count >= t.length) { return false; }

                while(current_entry < t.length && (t[current_entry] == null || t[current_entry].isDeleted()))
                {
                    current_entry++;
                }
                if(current_entry >= t.length) return false;
                return !t[current_entry].isDeleted();
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public V next()
            {
                // control: fail-fast iterator...
                if(TSBHashTableDA.this.modCount != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                TSBEntryDA<K, V> t = TSBHashTableDA.this.table[current_entry];
                current_entry++;
                next_ok = true;
                return t.getValue();
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove()
            {
                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                TSBEntryDA<K, V> garbage = TSBHashTableDA.this.table[current_entry];
                garbage.delete();

                next_ok = false;
                TSBHashTableDA.this.count--;
                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }
}
