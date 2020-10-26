package com.prathyusha.config;

import java.io.IOException;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.prathyusha.batch.DBWriter;

import com.prathyusha.batch.Processor;
import com.prathyusha.model.Person;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
	
	
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private DBWriter dbwriter;
	
	@Autowired
	private Processor process;
	
	static Resource[] resources;

	public BatchConfig(Resource[] resources) {
		super();
		this.resources = resources;
		System.out.println("res in constructor " + resources[0].getFilename());
		getResources();
	}

	public Resource[] getResources() {

		System.out.println("res in getter " + resources[0].getFilename());
		return resources;
	}

	public void setResources(Resource[] resources) {
		this.resources = resources;
	}

	public BatchConfig() {
		super();
	}
	
	@Bean

	public Job job() {
		return jobBuilderFactory.get("job").incrementer(new RunIdIncrementer()).start(step()).build();
	}
	
	
	@Bean

	public Step step() {
		return stepBuilderFactory.get("step").<Person, Person>chunk((3))

				.reader(itemReader())

				.processor(process)

				.writer(dbwriter).taskExecutor(taskExecutor()).build();
	}
	
	
	
	@Bean
	@Qualifier
	@StepScope
	public MultiResourceItemReader<Person> itemReader() {
		MultiResourceItemReader<Person> resourceItemReader = new MultiResourceItemReader<Person>();

		ClassLoader cl = this.getClass().getClassLoader();
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
		// Resource[] res = null;

		try {
			
			Resource[] resources = resolver.getResources("file:e:/csvfiles/person*.csv");
			for (Resource resource : resources) {
				System.out.println("file names :" + resource.getFilename());
				
			}
			resourceItemReader.setResources(resources);
			resourceItemReader.setDelegate(reader());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("new file " + resourceItemReader.getCurrentResource());

		return resourceItemReader;
	}
	
	
	@Bean
	@Primary
	// @StepScope
	public FlatFileItemReader<Person> reader() {
		// Create reader instance
		FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
		// Set number of lines to skips. Use it if file has header rows.
		System.out.println("file names " + reader.toString());
		//reader.setLinesToSkip(1);
		System.out.println(reader.toString());
		// Configure how each line will be parsed and mapped to different values
		reader.setLineMapper(new DefaultLineMapper<Person>() {
			{
				// 3 columns in each row
				setLineTokenizer(new DelimitedLineTokenizer() {
					{
						
						setNames(new String[] { "id", "name", "phoneNo" });
					}
				});
				// Set values in Person class
				setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {
					{
						setTargetType(Person.class);
					}
				});
			}
		});
		return reader;
	}
	
	

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize(10);
		taskExecutor.afterPropertiesSet();
		taskExecutor.getActiveCount();
        return taskExecutor;
	}


}
