package com.analisis.algoritmos.algorithms.sorting;

import org.springframework.stereotype.Component;

import java.util.Comparator;

/**
 * Implementación manual del algoritmo Tree Sort usando un BST (Binary Search Tree).
 *
 * Estrategia:
 * 1. Insertar todos los elementos del array en un BST.
 * 2. Realizar un recorrido inorden (in-order traversal) del BST.
 * 3. El recorrido inorden produce los elementos en orden ascendente.
 *
 * A diferencia de java.util.TreeSet, esta implementación SOPORTA DUPLICADOS
 * almacenándolos en el subárbol derecho (o mediante un contador por nodo).
 *
 * Complejidad:
 * - Mejor caso: O(n log n) — árbol balanceado.
 * - Caso promedio: O(n log n)
 * - Peor caso: O(n²) — árbol degenerado (datos ya ordenados).
 * - Espacio: O(n) — nodos del árbol.
 *
 * @author David
 * @since 1.0.0
 */
@Component
public class TreeSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "Tree Sort";
    }

    @Override
    public String getComplexity() {
        return "O(n log n)";
    }

    @Override
    public String getBestCase() {
        return "O(n log n)";
    }

    @Override
    public String getWorstCase() {
        return "O(n²)";
    }

    @Override
    public String getAverageCase() {
        return "O(n log n)";
    }

    @Override
    public <T extends Comparable<T>> void sort(T[] array) {
        sort(array, Comparator.naturalOrder());
    }

    @Override
    public <T> void sort(T[] array, Comparator<T> comparator) {
        if (array == null || array.length <= 1) {
            return;
        }

        // Construir BST insertando todos los elementos
        BSTNode<T> root = null;
        for (T element : array) {
            root = insert(root, element, comparator);
        }

        // Recorrido inorden para extraer elementos en orden
        int[] index = {0}; // Usamos array de 1 elemento para mantener referencia mutable
        inorderTraversal(root, array, index);
    }

    /**
     * Nodo del Binary Search Tree (BST).
     * Soporta duplicados: elementos iguales se almacenan en el subárbol derecho.
     */
    private static class BSTNode<T> {
        T value;
        BSTNode<T> left;
        BSTNode<T> right;

        BSTNode(T value) {
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }

    /**
     * Inserta un valor en el BST.
     *
     * - Si el valor es menor que el nodo actual, va al subárbol izquierdo.
     * - Si el valor es mayor o IGUAL, va al subárbol derecho (soporta duplicados).
     *
     * Complejidad: O(h) donde h = altura del árbol.
     *
     * @param node Nodo raíz del subárbol actual
     * @param value Valor a insertar
     * @param comparator Comparador para determinar orden
     * @return Nodo raíz actualizado
     */
    private <T> BSTNode<T> insert(BSTNode<T> node, T value, Comparator<T> comparator) {
        if (node == null) {
            return new BSTNode<>(value);
        }

        int cmp = comparator.compare(value, node.value);

        if (cmp < 0) {
            node.left = insert(node.left, value, comparator);
        } else {
            // >= 0: duplicados van al subárbol derecho para preservarlos
            node.right = insert(node.right, value, comparator);
        }

        return node;
    }

    /**
     * Recorrido inorden del BST: izquierda → nodo → derecha.
     * Produce los elementos en orden ascendente.
     *
     * Complejidad: O(n) — visita cada nodo exactamente una vez.
     *
     * @param node Nodo actual del recorrido
     * @param array Array destino donde depositar los elementos ordenados
     * @param index Índice actual en el array (referencia mutable)
     */
    private <T> void inorderTraversal(BSTNode<T> node, T[] array, int[] index) {
        if (node == null) {
            return;
        }

        inorderTraversal(node.left, array, index);
        array[index[0]++] = node.value;
        inorderTraversal(node.right, array, index);
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public boolean isInPlace() {
        return false;
    }
}
