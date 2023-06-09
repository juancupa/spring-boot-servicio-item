package com.formacionbdi.springboot.app.item.controllers;


import java.util.HashMap;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.formacionbdi.springboot.app.item.models.Item;
import com.formacionbdi.springboot.app.item.models.Producto;
import com.formacionbdi.springboot.app.item.models.service.ItemService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.vavr.collection.Map;
import ch.qos.logback.classic.Logger;

@RestController
public class ItemController {
	
	//@Autowired
	//private final CircuitBreakerFactory cbFactory;
	
	private final Logger logger= (Logger) LoggerFactory.getLogger(ItemController.class);
	
	
	@Autowired
	private Environment env;
	
	
	@Autowired
	@Qualifier("serviceFeign")
	private ItemService itemService;
	
	@Value("${configuracion.texto}")
	private String texto;
	
	@GetMapping("/listar")
	public List<Item> listar(@RequestParam(name="nombre", required= false) String nombre, @RequestHeader(name="token-request", required= false) String token){
		System.out.println(nombre);
		System.out.println(token);
		System.out.println(nombre);
		return itemService.findAll();
	}

	
	/**@GetMapping("ver/{id}/cantidad/{cantidad}")
	public Item detalle(@PathVariable Long id, @PathVariable Integer cantidad) {
		return itemService.findById(id, cantidad);
	}**/
	
	
	@GetMapping("ver/{id}/cantidad/{cantidad}")
	@CircuitBreaker(name = "items", fallbackMethod = "metodoAlternativo")
	public Item detalle(@PathVariable Long id, @PathVariable Integer cantidad) {
		
		
		try {
			/**Llamada a un serviico externo */
			return itemService.findById(id, cantidad);

		} catch (Exception e) {
			throw new RuntimeException("Error al obtener el item:" + e.getMessage() );
		}
			
	}
	
	
	public Item metodoAlternativo(Long id, Integer cantidad,Throwable  e) {
		
		logger.info(e.getMessage());
		Item item = new Item();
		Producto producto = new Producto();
		
		item.setCantidad(cantidad);
		producto.setId(id);
		producto.setNombre("Camara Sony");
		producto.setPrecio(500.00);
		item.setProducto(producto);
		return item;
	}
	
	/*@GetMapping("/obtener-config")
	public ResponseEntity<?> obtenerConfig(@Value("${server.port}") String puerto){
		Map<String,String> json =  new HashMap<>();
		json.put("texto",texto);
		json.put("puerto",puerto);
		//return new ResponseEntity<Map<String, String>>(json, HttpStatus.OK);
		return new ResponseEntity<>(json, HttpStatus.OK);
		
		
	}*/
	
	@GetMapping("/obtener-config")
	public ResponseEntity<HashMap<String, String>> obtenerConfig(@Value("${server.port}") String puerto) {
	    HashMap<String, String> json = new HashMap<>();
	    json.put("texto", texto);
	    json.put("puerto", puerto);
	    
	    if(env.getActiveProfiles().length>0 && env.getActiveProfiles()[0].equals("dev"))  {
	    	json.put("autor.nombre", env.getProperty("configuracion.autor.nombre"));
	    	json.put("autor.email", env.getProperty("configuracion.autor.email"));
	    }
	    return new ResponseEntity<>(json, HttpStatus.OK);
	}
}
