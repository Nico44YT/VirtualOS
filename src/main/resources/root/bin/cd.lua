function execute(fileSystem, currentDir, arguments)
    fileSystem:changeDirectory(arguments)
end

function description()
    return "Move through the filesystem"
end