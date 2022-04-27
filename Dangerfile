require 'json'

puts "Starting danger"

total_dependencies_count = 0
outdated_dependencies_count = 0

dependenciesReportFile = "./build/dependencyUpdates/report.json"

if (File.exist?(dependenciesReportFile))
    rawDependenciesReport = File.read(dependenciesReportFile)
    dependencies = JSON.parse(rawDependenciesReport)
    total_dependencies_count = dependencies['count']
    outdated_dependencies_count = dependencies['outdated']['count']
end

mobiledevops_json = {
  "dependencies": {
    "count": total_dependencies_count,
    "outdated_count": outdated_dependencies_count
  },
}

puts mobiledevops_json
