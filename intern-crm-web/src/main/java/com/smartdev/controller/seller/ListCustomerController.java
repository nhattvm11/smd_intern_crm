package com.smartdev.controller.seller;

import com.smartdev.crm.service.HistoryAdvisoryService;
import com.smartdev.crm.service.StatusService;
import com.smartdev.user.entity.Customer;
import com.smartdev.crm.service.CustomerService;
import com.smartdev.user.entity.HistoryAdvisory;
import com.smartdev.user.entity.HistoryTest;
import com.smartdev.user.entity.Status;
import com.smartdev.user.model.CustomerRespon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/seller")
public class ListCustomerController {
    @Autowired
    CustomerService customerService;

    @Autowired
    HistoryAdvisoryService historyAdvisoryService;

    @Autowired
    StatusService statusService;

    @RequestMapping(value = "/list-custom", method = RequestMethod.GET)
    public String listCustom(Model model, @RequestParam(required = false) Integer statusId,
                             @RequestParam(required =false) Integer productType,
                             @RequestParam(required = false ,defaultValue = "1") Integer pageNum) {
        model.addAttribute("list", customerService.listCustomerWithFilter(statusId, productType, pageNum));
        return "list-custom";
    }

    @RequestMapping(value = "/advisory", method = RequestMethod.GET)
    public ModelAndView advisory(@RequestParam("customer") Integer theId) {
        Customer customer = customerService.findOneid(theId);
        List<HistoryAdvisory> historyAdvisories = historyAdvisoryService
                .getHistoryAdvisoriesByCustomer(customer);
        ModelAndView modelAndView = new ModelAndView("advisory");
        modelAndView.addObject("getItemCustomer", customer);
        modelAndView.addObject("historyAdvisories", historyAdvisories);


        //create new historytest
        HistoryTest historyTest = new HistoryTest();

        //get time now
        Timestamp currentDate = new Timestamp(new Date().getTime());

        //set time now to object
        historyTest.setDate(currentDate);

        //set customerId to object
        historyTest.setCustomerId(theId);
        //
        //get all status
        List<Status> statuses = statusService.findAll();
        modelAndView.addObject("listStatus", statuses);

        System.out.println("hhhhh" + historyTest.getStatus());

        //send object to view
        modelAndView.addObject("history", historyTest);


        return modelAndView;
    }

    @RequestMapping(value = "/saveHistoryAdvisory", method = RequestMethod.POST)
    public String saveHistoryAdvisory(@ModelAttribute("history") HistoryTest historyTest) {
        //create new HistoryAdvisory
        HistoryAdvisory historyAdvisory = new HistoryAdvisory();

        //get value status
        Status status = statusService.findById(historyTest.getStatus());

        //set status to historyAdvisory
        historyAdvisory.setStatusByStatusId(status);

        //set date to historyAdvisory
        historyAdvisory.setDate(historyTest.getDate());

        //set comment to historyAdvisory
        historyAdvisory.setComment(historyTest.getComment());

        //get id of customer and set status of customer
        Customer customer = customerService.findOneid(historyTest.getCustomerId());
        historyAdvisory.setCustomerByCustomerId(customer);
        customer.setStatusByStatusId(status);

        //save data historyAdvisor
        historyAdvisoryService.save(historyAdvisory);

        //save status customer
        customerService.saveCustomer(customer);

        historyAdvisory.toString();
        return "redirect:/seller/list-custom-seller";
    }

    @RequestMapping(value = "/addcustomer", method = RequestMethod.GET)
    public ModelAndView addCustomer() {
        ModelAndView modelAndView = new ModelAndView("addcustomer");
        modelAndView.addObject("customer", new Customer());
        return modelAndView;
    }

    @RequestMapping(value = "/addcustomer", method = RequestMethod.POST)
    public String handleAddCustomer(@ModelAttribute("customer") @Valid Customer customer,
                                    BindingResult result) {
        if (result.hasErrors()) {
            return "addcustomer";
        }
        customerService.addCustomer(customer);
        return "redirect:/seller/list-custom-seller";
    }

    @RequestMapping(value = "/editcustomer/{id}", method = RequestMethod.GET)
    public ModelAndView getEditCustomer(@PathVariable(value = "id") Integer id) {
        Customer customer = customerService.findOneid(id);
        ModelAndView modelAndView = new ModelAndView("editcustomer");
        modelAndView.addObject("getItemCustomer", customer);
        return modelAndView;
    }

    @RequestMapping(value = "/editcustomer/{id}", method = RequestMethod.POST)
    public String postEditCustomer(@ModelAttribute("getItemCustomer") @Valid Customer customer,
                                   BindingResult result) {
        if (result.hasErrors()) {
            return "editcustomer";
        }
        Customer customerOld = customerService.findOneid(customer.getId());
        customerOld.setAge(customer.getAge());
        customerOld.setName(customer.getName());
        customerOld.setCompany(customer.getCompany());
        customerOld.setFacebook(customer.getFacebook());
        customerOld.setMail(customer.getMail());
        customerOld.setPhone(customer.getPhone());
        customerOld.setProductType(customer.getProductType());
        customerService.saveCustomer(customerOld);
        return "redirect:/seller/list-custom-seller";
    }

    @RequestMapping(value = "/list-custom-filter", method = RequestMethod.GET, headers =
            {"Accept=text/xml, " +
            "application/json"})
    @ResponseBody
    public ResponseEntity<CustomerRespon> listCustomerByFilters(@RequestParam Integer statusId,
                                                                @RequestParam Integer
            productType) {
        List<Customer> customerList = new ArrayList<>();
        List<CustomerRespon> customerRespons = new ArrayList<>();
        if (statusId == 0 && productType == 0) {
//            return customerService.findByProductTypeAndStatusByStatusId(productType,null);
        } else if (statusId > 0) {
            Status status = statusService.findById(statusId);
//            customerList = customerService.findCustomersByStatusId(status);
        } else if (productType > 0) {
//            return customerService.findCustomersByProductType(productType);
        } else {
//            return listCustomManageService.listAllCustomer();
        }
        for (Customer cus : customerList) {
            CustomerRespon customerRespon = new CustomerRespon(cus);
            customerRespons.add(customerRespon);
        }
        return new ResponseEntity<>(customerRespons.get(0), HttpStatus.OK);

    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String delete(@RequestParam("customer") Integer theId) {
        Customer customer = customerService.findOneid(theId);
        customer.setIsDelete(1);
        customerService.saveCustomer(customer);

        return "redirect:/seller/list-custom-seller";
    }

}
