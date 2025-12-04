function execute(fileSystem, currentDir, arguments)
    print(arguments)
    fileSystem:createDirecotry(currentDir, arguments)
end

function description()
    return "Creates a directory"
end