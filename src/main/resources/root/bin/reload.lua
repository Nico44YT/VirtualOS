function execute(fileSystem, currentDir, arguments)
    fileSystem:reload()
    print("Reloaded System Commands")
end

function description()
    return "Reloads all commands"
end