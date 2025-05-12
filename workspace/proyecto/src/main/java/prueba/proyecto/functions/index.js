
//Esto es un ejemplo de un backend hecho a partir de las herramientas de firebase, 

const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

// Crear un nuevo cliente
exports.crearCliente = functions.https.onRequest(async (req, res) => {
  if (req.method !== 'POST') return res.status(405).send('Método no permitido');

  const { nombre, email } = req.body;

  if (!nombre || !email) return res.status(400).send('Faltan datos');

  try {
    const nuevoCliente = await db.collection('clientes').add({ nombre, email });
    res.status(201).send({ id: nuevoCliente.id, mensaje: 'Cliente creado' });
  } catch (error) {
    console.error(error);
    res.status(500).send('Error interno al crear cliente');
  }
});

// Crear una cita asociada a un cliente
exports.crearCita = functions.https.onRequest(async (req, res) => {
  if (req.method !== 'POST') return res.status(405).send('Método no permitido');

  const { clienteId, fecha, descripcion } = req.body;

  if (!clienteId || !fecha || !descripcion) {
    return res.status(400).send('Faltan datos');
  }

  try {
    const cita = { fecha, descripcion };
    await db.collection('clientes')
            .doc(clienteId)
            .collection('citas')
            .add(cita);

    res.status(201).send({ mensaje: 'Cita agregada correctamente' });
  } catch (error) {
    console.error(error);
    res.status(500).send('Error interno al crear cita');
  }
});
