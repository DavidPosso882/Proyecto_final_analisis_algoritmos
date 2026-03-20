#!/bin/bash

echo "El servidor se iniciará en:"
echo "   http://localhost:3000"
echo ""
echo "Comandos útiles:"
echo "   - Abrir en Chrome:  xdg-open http://localhost:3000"
echo "   - Abrir en Firefox: firefox http://localhost:3000"
echo "   - Detener servidor:  Ctrl+C"
echo ""
echo "Iniciando servidor de desarrollo..."
echo ""

# Iniciar el servidor sin abrir navegador
BROWSER=none npm run start
