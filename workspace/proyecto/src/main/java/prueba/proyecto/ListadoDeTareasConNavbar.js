const menuIcon = document.getElementById("menuIcon");
const navLinks = document.getElementById("navLinks");
const userMenuItems = document.getElementById("userLinks")

	menuIcon.addEventListener("click", () => {
    navLinks.classList.toggle("active");
    userMenuItems.classList.toggle("active");
  });

// Opcional: cerrar el menú cuando haces clic fuera (experiencia mejorada)
document.addEventListener("click", (e) => {
  if (!menuIcon.contains(e.target) && !navLinks.contains(e.target)) {
    navLinks.classList.remove("active");
  }
});

// Opcional: cerrar el menú cuando haces clic fuera (experiencia mejorada)
document.addEventListener("click", (e) => {
  if (!menuIcon.contains(e.target) && !navLinks.contains(e.target)) {
    navLinks.classList.remove("active");
  }
});

const { createApp } = Vue;

createApp({
  data() {
    return {
      tasks: [],
      showPopup: false,
      isEditing: false,
      editIndex: null,
      currentTask: {
        nombre: '',
        fecha: '',
        prioridad: ''
      }
    };
  },
  methods: {
    openAddPopup() {
      this.isEditing = false;
      this.currentTask = { nombre: '', fecha: '', prioridad: '' };
      this.showPopup = true;
    },
    addTask() {
      this.tasks.push({ ...this.currentTask });
      this.closePopup();
    },
    editTask(index) {
      this.isEditing = true;
      this.editIndex = index;
      this.currentTask = { ...this.tasks[index] };
      this.showPopup = true;
    },
    updateTask() {
      this.tasks[this.editIndex] = { ...this.currentTask };
      this.closePopup();
    },
    deleteTask(index) {
      if (confirm("¿Seguro que quieres eliminar esta tarea?")) {
        this.tasks.splice(index, 1);
      }
    },
    closePopup() {
      this.showPopup = false;
    }
  }
}).mount('#app');

