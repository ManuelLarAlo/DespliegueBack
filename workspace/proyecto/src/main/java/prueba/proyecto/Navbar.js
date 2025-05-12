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
