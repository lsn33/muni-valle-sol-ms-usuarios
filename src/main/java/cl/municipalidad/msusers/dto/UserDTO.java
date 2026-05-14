package cl.municipalidad.msusers.dto;

public record UserDTO(
    Long id,
    String nombre,
    String email,
    String rol,
    Boolean activo
) {}

//aqui cumplimos con el record pattern, ya que UsuarioDTO es un record que encapsula los datos de un usuario de manera inmutable, 
// proporcionando una forma sencilla y eficiente de transferir datos entre capas de la aplicación sin exponer detalles de implementación.