package backvisca;


import backvisca.entity.Commands;
import backvisca.entity.Macro;
import backvisca.jsonmacros.MacroReader;
import backvisca.jsonmacros.MacroWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController()
@RequestMapping("visca")
public class ViscaResource {

    private final ViscaService viscaService;

    private final MacroWriter macroWriter;

    private final MacroReader macroReader;

    @Autowired
    public ViscaResource(ViscaService viscaService, MacroWriter macroWriter, MacroReader macroReader) {
        this.viscaService = viscaService;
        this.macroWriter = macroWriter;
        this.macroReader = macroReader;
    }

    @PostMapping("execute")
    public ResponseEntity<List<String>> sendCommand(@RequestBody Commands commands){
        List<String> strings = viscaService.sendCommands(commands);
        return ResponseEntity.ok(strings);
    }

    @PostMapping(value = "macro",  produces = "application/json")
    public ResponseEntity<Macro> saveMacro(@RequestBody Macro macro){
        Macro result = macroWriter.save(macro);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "macro", produces = "application/json")
    public ResponseEntity<List<Macro>> getAll(){
        List<Macro> commands = macroReader.getAll();
        return ResponseEntity.ok(commands);
    }

    @PostMapping(value = "macro/execute/{id}")
    public ResponseEntity<List<String>> executeMacro(@PathVariable("id") int id){
        List<Macro> macros = macroReader.getAll();
        Optional<Macro> macro = macros.stream().filter(m -> m.getId() == id).findFirst();
        if(macro.isPresent()){
            Commands commands = new Commands(macro.get().getAddress(), macro.get().getCommands());
            List<String> strings = viscaService.sendCommands(commands);
            return ResponseEntity.ok(strings);
        }
        return ResponseEntity.status(404).build();
    }
}
