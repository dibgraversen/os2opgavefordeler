Vagrant.configure(2) do |config|
    config.vm.box = "centos/7"

    config.vm.provision :shell, inline: 'rpm -ivh https://yum.puppetlabs.com/puppetlabs-release-el-7.noarch.rpm; yum -y install puppet'
    config.vm.provision :puppet do |puppet|
        puppet.manifests_path = "environment/puppet/manifests"
        puppet.manifest_file = "site.pp"
        puppet.module_path = "environment/puppet/modules"
    end
    config.vm.provision :shell, inline: 'chmod a+x /home/vagrant'

    config.vm.network "forwarded_port", guest: 80, host: 1080
    config.vm.network "forwarded_port", guest: 81, host: 1081
    config.vm.network "forwarded_port", guest: 22, host: 2223
    config.vm.network "forwarded_port", guest: 5432, host: 5432
    config.vm.network "forwarded_port", guest: 8080, host: 8080
    config.vm.network "forwarded_port", guest: 8787, host: 8787
    config.vm.network "forwarded_port", guest: 9990, host: 9990

end
