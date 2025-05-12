<template>
  <nav class="navbar">
    <div class="logo">
      <a href="#">Mi Aplicación</a>
    </div>
    <!-- Menú de navegación -->
    <ul class="nav-links" :class="{ active: menuActive }">
      <li><a href="#">Inicio</a></li>
      <li><a href="#">Acerca de</a></li>
      <li><a href="#">Servicios</a></li>
      <li><a href="#">Contacto</a></li>
      <!-- Menú de usuario con clase 'user-menu-item' -->
      <li class="user-menu-item"><a href="#">Configurar usuario</a></li>
      <li class="user-menu-item"><a href="#">Cerrar sesión</a></li>
    </ul>
    <div class="menu-icon" @click="toggleMenu">
      <span>&#9776;</span>
    </div>
  </nav>
</template>

<script>
export default {
  data() {
    return {
      // Estado reactivo para saber si el menú está activo
      menuActive: false
    };
  },
  methods: {
    // Método para alternar el estado del menú
    toggleMenu() {
      this.menuActive = !this.menuActive;
    }
  },
  mounted() {
    // Cerrar el menú si se hace clic fuera del mismo
    document.addEventListener("click", this.handleClickOutside);
  },
  beforeDestroy() {
    // Eliminar el listener cuando el componente se destruya
    document.removeEventListener("click", this.handleClickOutside);
  },
  methods: {
    toggleMenu() {
      this.menuActive = !this.menuActive;
    },
    handleClickOutside(event) {
      const menuIcon = this.$refs.menuIcon;
      const navLinks = this.$refs.navLinks;
      if (!menuIcon.contains(event.target) && !navLinks.contains(event.target)) {
        this.menuActive = false;
      }
    }
  }
};
</script>

<style scoped>
body {
  font-family: Arial, sans-serif;
  background-color: #f9f9f9;
  margin: 0;
}

/* Navbar Styles */
.navbar {
  display: flex;
  justify-content: space-between;
  background-color: #333;
  padding: 10px 20px;
  color: white;
  position: relative;
  align-items: center;
}

.logo a {
  color: white;
  text-decoration: none;
  font-size: 24px;
}

/* Menú de navegación siempre visible en pantallas grandes y dentro del menú hamburguesa en pantallas pequeñas */
.nav-links {
  list-style: none;
  display: flex;
  gap: 20px;
  flex-direction: row;
}

.nav-links li a {
  color: white;
  text-decoration: none;
  font-size: 18px;
}

.nav-links li a:hover {
  text-decoration: underline;
}

.menu-icon {
  display: none;
  font-size: 30px;
  color: white;
  cursor: pointer;
}

/* Responsive Navbar */
@media (max-width: 600px) {
  /* Menú de navegación se oculta en pantallas pequeñas */
  .nav-links {
    display: none;
    flex-direction: column;
    width: 100%;
    position: absolute;
    top: 60px;
    left: 0;
    background-color: rgba(51, 51, 51, 0.8);
    padding: 10px 0;
    z-index: 1000;
    border: 1px solid white;
  }

  /* Mostrar el menú cuando está activo */
  .nav-links.active {
    display: flex;
  }

  /* Mostrar el icono del menú hamburguesa */
  .menu-icon {
    display: block;
  }

  /* Estilo para cada enlace en el menú hamburguesa */
  .nav-links li {
    text-align: center;
    padding: 10px 0;
    width: 100%;
  }

  .nav-links li a {
    font-size: 20px;
  }
}

@media (min-width: 601px) {
  /* En pantallas grandes, las opciones de usuario están en el mismo menú hamburguesa */
  .menu-icon {
    display: block;
  }

  .nav-links {
    display: flex;
    gap: 20px;
    flex-direction: row;
    width: auto; /* Asegura que no se expanda innecesariamente */
  }

  /* No se requiere la clase active aquí porque el menú está visible por defecto en pantallas grandes */
  .nav-links li {
    text-align: center;
  }

  /* Si deseas que el tamaño de los enlaces cambie en pantallas grandes, puedes hacerlo aquí */
  .nav-links li a {
    font-size: 18px;
  }

  /*Oculta los elementos de usuario de la navbar en pantallas grandes*/
  .user-menu-item {
    display: none;
  }
}
</style>
